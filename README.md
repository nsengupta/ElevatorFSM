Well, the Internet is full of articles/blogs/code that explain/demonstrate how to model
an elevator's functions as a Finite State Machine (FSM). What the heck!
I thought of adding one more. :-)

But, my intention is not to repeat the old story. I want to try and model the following,
using one elevator as an unit:

- Behaviour of a single elevator (or a lift, as is referred to in my part of the world)
- Controlling its behaviour from a, well, Controller
- Modeling a collection of lifts, as is seen in hotels
- Coordination between these lifts and the controller
- Accounting for h/w devices in the lifts, which send asynchronous signals (messages) to the lifts they are attached to

I don't expect to complete this at one go. Instead, I will build it phase-wise.

The blog is [here](http://blogoloquy.blogspot.in/2016/11/finite-state-machine-using-akka.html).

A few hints about various components (may help follow the implementation)

- org.nirmalya.carriages.types.withExplicitMovingState.LiftCarriageWithMovingState

It models the carriage itself; constructed with a _TimeConsumingHWSignalSimulator_

- org.nirmalya.entities.TimeConsumingHWSignalSimulator

It implements the _MovingStateSimulator_ trait. Specifically, it implements the simulation of 
spending time while moving to a particular floor (function: simulateMovementTo()).

- org.nirmalya.entities.InlaidButtonPanel

It models a button panel mounted inside a carriage. It is identified by an ID as well as the
carriage it is mounted inside. The carriage is an actor; hence, it is constructed with an
_ActorRef_.

- org.nirmalya.entities.LiftController

The Controller in the system. It is constructed with a collection of carriages.