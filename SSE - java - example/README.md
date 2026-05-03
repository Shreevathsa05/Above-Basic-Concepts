# Server-Sent Events (SSE) in Spring Boot

This repository provides a conceptually rich, example-based guide to implementing **Server-Sent Events (SSE)** using Java and Spring Boot. 

SSE is a server push technology enabling a client to receive automatic updates from a server via an HTTP connection. Unlike WebSockets, SSE is **unidirectional** (Server -> Client), making it perfect for real-time status updates, news feeds, and live notifications without the overhead of full-duplex communication.

This project demonstrates two different approaches to implementing SSE in Spring Boot:
1. **Using Spring WebFlux (`Flux`)** for reactive streaming.
2. **Using Spring Web MVC (`SseEmitter`)** for managing asynchronous background job statuses.

---

## Concepts Covered

- **What is SSE?** How it differs from WebSockets and Long-Polling.
- **`EventSource` API:** How the browser connects to an SSE endpoint and listens for incoming messages.
- **Reactive Streams:** Using WebFlux's `Flux` to push periodic events.
- **`SseEmitter`:** Handling asynchronous task updates mapping to specific clients/jobs.

---

##  Project Structure

### 1. Basic Reactive SSE Stream (`SSEBasic.java`)
This example uses **Spring WebFlux**.
- **Endpoint:** `GET /api/v1/stream`
- **Concept:** It returns a `Flux<String>` that emits a new event ("Hello - [iteration]") every 2 seconds. `Flux` naturally maps to SSE streams when the browser requests `text/event-stream`.
- **Client (UI):** `index.html` + `script.js`
  - Clicking the "Click me" button opens an `EventSource` connection.
  - The client simply listens to `onmessage` and logs the continuous stream of events to the console.

### 2. Job Status Tracker (`SSEStatusControl.java`)
This example uses **Spring Web MVC's `SseEmitter`**.
- **Endpoints:** 
  - `GET /api/v1/status/subscribe/{jobId}`: Opens the SSE connection for a specific job.
  - `POST /api/v1/status/start/{jobId}`: Triggers an asynchronous background thread simulating a long-running process.
- **Concept:** This demonstrates a practical real-world scenario—tracking a background job's progress. We store active `SseEmitter` instances in a `ConcurrentHashMap`. As the background thread progresses through stages (`PENDING` -> `UPLOADING` -> `PROCESSING` -> `COMPLETED`), it looks up the emitter by `jobId` and sends status updates directly to the client.
- **Client (UI):** `status.html` + `status.js`
  - The user opens a connection (`EventSource`).
  - The user starts a job via a POST request.
  - The UI updates dynamically (e.g., `PROCESSING 50%...`) as events are pushed from the server.
  - Once the `COMPLETED` event is received, the client closes the connection.

---

## How to Run and Test

1. **Start the Application:** Run `SseApplication.java`. The server starts on default port (usually `8080`).
2. **Test Basic Streaming (WebFlux):**
   - Navigate to `http://localhost:8080/`
   - Open your browser's Developer Tools (Console).
   - Click **"Click me"**.
   - Observe the server pushing messages to the console every 2 seconds.
3. **Test Job Status Tracking (SseEmitter):**
   - Navigate to `http://localhost:8080/status`
   - Click **"Open Connection"** to subscribe to job updates.
   - Click **"Start Job"** to trigger the backend process.
   - Watch the UI dynamically update its text to show the job's progress from `PENDING` to `COMPLETED`.

---

## 🧠 Why SSE over WebSockets?
* **Simpler Protocol:** SSE uses standard HTTP. No custom protocol handling is required.
* **Built-in Reconnection:** The browser's `EventSource` API handles dropped connections and automatic retries out of the box.
* **Lightweight:** Ideal for scenarios where you only need data flowing in one direction (Server -> Client), such as live sports scores, progress bars, or stock tickers.

## ⚠️ Important Considerations
* **Unidirectional:** The client cannot send messages back over the same connection.
* **Browser Limits:** Browsers typically limit the number of open SSE connections to the same domain (often 6 connections for HTTP/1.1). Using HTTP/2 heavily mitigates this issue.
