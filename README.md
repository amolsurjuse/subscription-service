# subscription-service

Electra Hub subscription management service.

## What it manages
- Subscription plans with overall fee discounts and session-fee discounts
- Optional quota limits per plan or allocation
- Allocations to users, organizations, or organization groups
- Utilization audit logs per user with fee breakdowns and applied discounts

## Endpoints
- `POST /api/v1/subscriptions/plans`
- `GET /api/v1/subscriptions/plans`
- `GET /api/v1/subscriptions/plans/{planId}`
- `POST /api/v1/subscriptions/allocations`
- `GET /api/v1/subscriptions/allocations`
- `PATCH /api/v1/subscriptions/allocations/{allocationId}/status`
- `POST /api/v1/subscriptions/utilizations/preview`
- `POST /api/v1/subscriptions/utilizations`
- `GET /api/v1/subscriptions/utilizations`
- `GET /api/v1/subscriptions/audit-logs`

## Local run
```bash
./mvnw spring-boot:run
```

## TeamCity pipeline
Provision the TeamCity pipeline with:

```bash
TEAMCITY_TOKEN='<token>' ./ci/teamcity/setup_pipeline.sh
```
