package com.electrahub.subscription.grpc;

import com.electrahub.proto.subscription.v1.SubscriptionPlanServiceGrpc;
import com.electrahub.proto.subscription.v1.CreateSubscriptionPlanRequest;
import com.electrahub.proto.subscription.v1.ListSubscriptionPlansRequest;
import com.electrahub.proto.subscription.v1.GetSubscriptionPlanRequest;
import com.electrahub.proto.subscription.v1.SubscriptionPlanResponse;
import com.electrahub.proto.subscription.v1.SubscriptionPlanListResponse;
import com.electrahub.subscription.api.dto.CreateSubscriptionPlanRequest;
import com.electrahub.subscription.api.dto.SubscriptionPlanResponse;
import com.electrahub.subscription.api.dto.SubscriptionPlanSearchResponse;
import com.electrahub.subscription.service.SubscriptionPlanService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class SubscriptionPlanGrpcService extends SubscriptionPlanServiceGrpc.SubscriptionPlanServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanGrpcService.class);

    private final SubscriptionPlanService subscriptionPlanService;

    public SubscriptionPlanGrpcService(SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @Override
    public void createPlan(
            CreateSubscriptionPlanRequest request,
            StreamObserver<SubscriptionPlanResponse> responseObserver
    ) {
        try {
            LOGGER.debug("gRPC: Creating subscription plan: {}", request.getName());

            com.electrahub.subscription.api.dto.CreateSubscriptionPlanRequest createRequest =
                    new com.electrahub.subscription.api.dto.CreateSubscriptionPlanRequest(
                            request.getName(),
                            request.getDescription(),
                            request.getFeatures()
                    );

            SubscriptionPlanResponse plan = subscriptionPlanService.create(createRequest);
            com.electrahub.proto.subscription.v1.SubscriptionPlanResponse response = convertToProto(plan);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument in createPlan", e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asException()
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error in createPlan", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .asException()
            );
        }
    }

    @Override
    public void listPlans(
            ListSubscriptionPlansRequest request,
            StreamObserver<SubscriptionPlanListResponse> responseObserver
    ) {
        try {
            LOGGER.debug("gRPC: Listing subscription plans with limit: {}, offset: {}",
                    request.getLimit(), request.getOffset());

            SubscriptionPlanSearchResponse searchResponse = subscriptionPlanService.list(
                    request.getLimit(),
                    request.getOffset()
            );

            SubscriptionPlanListResponse response = convertToListProto(searchResponse);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument in listPlans", e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asException()
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error in listPlans", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .asException()
            );
        }
    }

    @Override
    public void getPlan(
            GetSubscriptionPlanRequest request,
            StreamObserver<SubscriptionPlanResponse> responseObserver
    ) {
        try {
            LOGGER.debug("gRPC: Getting subscription plan: {}", request.getPlanId());

            SubscriptionPlanResponse plan = subscriptionPlanService.get(UUID.fromString(request.getPlanId()));
            com.electrahub.proto.subscription.v1.SubscriptionPlanResponse response = convertToProto(plan);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument in getPlan", e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asException()
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error in getPlan", e);
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Subscription plan not found")
                            .asException()
            );
        }
    }

    private com.electrahub.proto.subscription.v1.SubscriptionPlanResponse convertToProto(SubscriptionPlanResponse plan) {
        var builder = com.electrahub.proto.subscription.v1.SubscriptionPlanResponse.newBuilder()
                .setPlanId(plan.planId().toString())
                .setName(plan.name() != null ? plan.name() : "")
                .setDescription(plan.description() != null ? plan.description() : "");

        if (plan.features() != null) {
            plan.features().forEach(builder::addFeatures);
        }

        return builder.build();
    }

    private SubscriptionPlanListResponse convertToListProto(SubscriptionPlanSearchResponse searchResponse) {
        var builder = SubscriptionPlanListResponse.newBuilder();

        if (searchResponse.plans() != null) {
            searchResponse.plans().forEach(plan ->
                    builder.addPlans(convertToProto(plan))
            );
        }

        builder.setTotal(searchResponse.total());
        return builder.build();
    }
}
