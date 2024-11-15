// Agent alice

/* Initial beliefs */
!start.

/* Plans */

// Starting plan
+!start : true <-
    .print("Agent started...");
    !seek(agent2).

// Seek target agent
+!seek(Target) : true <-
    .print("Seeking ", Target);
    move.

// React when agents are neighbors
+neighbor(agent1, agent2) : true <-
    .print("Agent1 and Agent2 are neighbors.");
    greet("Hello from agent1!").

// Handle greeting acknowledgment
+greeted(Message) : true <-
    .print("Greeting acknowledged: ", Message).
