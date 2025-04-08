# Task Scheduler API Documentation

## Base URL

```
http://localhost:8080/api
```

## Task Model

```typescript
interface Task {
  id: number; // Unique identifier
  name: string; // Task name
  weight: number; // Task importance (1-10)
  dueDate: string; // ISO date format (YYYY-MM-DD)
  estimatedDuration: number; // Duration in hours
  dependenciesStr: string; // Comma-separated list of task IDs
}
```

## Endpoints

### 1. Create Task

```http
POST /tasks
```

Creates a new task in the system.

**Request Body:**

```json
{
  "name": "Complete Project Proposal",
  "weight": 8,
  "dueDate": "2025-04-06",
  "estimatedDuration": 2,
  "dependenciesStr": "1,2,3" // Optional, comma-separated task IDs
}
```

**Response:** `201 Created`

```json
{
  "id": 1,
  "name": "Complete Project Proposal",
  "weight": 8,
  "dueDate": "2025-04-06",
  "estimatedDuration": 2,
  "dependenciesStr": "1,2,3"
}
```

### 2. Get All Tasks

```http
GET /tasks
```

Retrieves all tasks in the system.

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "name": "Complete Project Proposal",
    "weight": 8,
    "dueDate": "2025-04-06",
    "estimatedDuration": 2,
    "dependenciesStr": "1,2,3"
  }
  // ... more tasks
]
```

### 3. Get Task by ID

```http
GET /tasks/{id}
```

Retrieves a specific task by its ID.

**Response:** `200 OK`

```json
{
  "id": 1,
  "name": "Complete Project Proposal",
  "weight": 8,
  "dueDate": "2025-04-06",
  "estimatedDuration": 2,
  "dependenciesStr": "1,2,3"
}
```

### 4. Update Task

```http
PUT /tasks/{id}
```

Updates an existing task.

**Request Body:**

```json
{
  "name": "Updated Project Proposal",
  "weight": 9,
  "dueDate": "2025-04-07",
  "estimatedDuration": 3,
  "dependenciesStr": "1,2,3,4"
}
```

**Response:** `200 OK`

```json
{
  "id": 1,
  "name": "Updated Project Proposal",
  "weight": 9,
  "dueDate": "2025-04-07",
  "estimatedDuration": 3,
  "dependenciesStr": "1,2,3,4"
}
```

### 5. Delete Task

```http
DELETE /tasks/{id}
```

Deletes a task from the system.

**Response:** `204 No Content`

### 6. Generate Schedule

```http
GET /tasks/schedule
```

Generates an optimal schedule based on all tasks in the system.

**Response:** `200 OK`

```json
{
  "schedule": [1, 2, 3, 4], // Array of task IDs in execution order
  "totalWeight": 35 // Total weight of scheduled tasks
}
```

## Error Responses

All endpoints may return the following errors:

```http
400 Bad Request
{
  "error": "Invalid request",
  "message": "Detailed error message"
}

404 Not Found
{
  "error": "Resource not found",
  "message": "Task with ID {id} not found"
}

500 Internal Server Error
{
  "error": "Server error",
  "message": "An unexpected error occurred"
}
```

## Task Validation Rules

1. `name`: Required, non-empty string
2. `weight`: Required, integer between 1 and 10
3. `dueDate`: Required, valid date in YYYY-MM-DD format
4. `estimatedDuration`: Required, positive integer (hours)
5. `dependenciesStr`: Optional, comma-separated list of existing task IDs

## Scheduling Algorithm Notes

The scheduling algorithm considers:

- Task weights (importance)
- Due dates (deadlines)
- Estimated durations
- Dependencies between tasks

The algorithm will:

1. Validate all dependencies
2. Calculate earliest possible start times
3. Ensure deadlines are met
4. Maximize total weight of scheduled tasks
5. Return tasks in optimal execution order

## Example Usage

### Creating a Task with Dependencies

```javascript
const task = {
  name: "Implement Feature X",
  weight: 7,
  dueDate: "2025-04-10",
  estimatedDuration: 4,
  dependenciesStr: "1,3", // Depends on tasks with IDs 1 and 3
};

fetch("http://localhost:8080/api/tasks", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify(task),
});
```

### Generating a Schedule

```javascript
fetch("http://localhost:8080/api/tasks/schedule")
  .then((response) => response.json())
  .then((data) => {
    const { schedule, totalWeight } = data;
    console.log("Optimal task order:", schedule);
    console.log("Total weight:", totalWeight);
  });
```
