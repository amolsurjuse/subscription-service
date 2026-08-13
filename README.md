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

## Free charging grants

The `FREE_CHARGING_USER_GRANT` (`Free Charging - All Fees`) plan is admin-only and covers 100% of energy,
time, connection/session, and idle fees for the granted kWh allowance. Taxes
remain payable. Allocate a controlled kWh allowance to one user with:

```http
POST /api/v1/admin/subscriptions/grants
Content-Type: application/json

{
  "planId": "7e3e15e8-3c0d-4b31-9bd6-2d2d2f3a5002",
  "userId": "00000000-0000-0000-0000-000000000001",
  "quotaValue": 100,
  "quotaUnit": "KWH",
  "grantReason": "Customer care credit",
  "createdBy": "admin@example.com"
}
```

Each grant is stored as a `USER` allocation and is independently auditable,
consumable, and revocable through the allocation status endpoint.

## Local run
```bash
./mvnw spring-boot:run
```

## TeamCity pipeline
Provision the TeamCity pipeline with:

```bash
TEAMCITY_TOKEN='<token>' ./ci/teamcity/setup_pipeline.sh
```
