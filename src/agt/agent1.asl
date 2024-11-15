// agent1.asl

!start.

// Plan to start the agent
+!start : true <-
    .print("Starting the agent...");
    greet("Hello from agent1");   // Directly invoking the 'greet' action
    move;                         // Directly invoking the 'move' action
    .print("Actions executed.").

// Optional: Handle feedback if the environment adds percepts
+greeted(Message) : true <-
    .print("Received feedback: ", Message).
