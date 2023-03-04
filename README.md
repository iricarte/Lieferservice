# Lieferservice

Lieferservice Projekt für Funktionale und objektorientierte Programmierkonzepte WiSe 2022/23

Autors:
I. Ricarte,
M. Ljungkrantz,
B. Kurth.

For the unedited German Version
see: https://raw.githubusercontent.com/FOP-2223/FOP-2223-TeX/main/FOP-2223-Projekt-Sheet.pdf

## Project Description:

You were asked by a well-known food delivery service to develop a program that allows you to simulate and
to simulate the process of a delivery service and to evaluate it at the end. Complete the following tasks in the template
the following tasks to complete the template that we provide to you. The template is divided into three
parts:
- Application: From here the actual simulation is started. Here you will implement the task H10.
ting.
- Domain: This is where the implementation of the basic problem is located. Except for task H10 and H11 you will
implement your solutions here.
- Infrastructure: This is where the communication with the "outside world" is located, i.e. the implementation of the GUI and IO
operations.

### Domain
The domain is built up in layers. The lowest layer is represented by
the interfaces region (see H2) and describes the structure of the delivery
of nodes (see H3) and edges (see H4) the structure of the delivery area.
area. The next layer is implemented by the interface VehicleManager (see H6).
and is responsible for managing the different vehicles (see H5) of the delivery service.
delivery service. On the third layer, incoming orders are accepted, managed and
orders are accepted, managed and assigned to the individual vehicles. This
is represented by the interface DeliveryService (see H9). On the last
layer is the Simulation interface, which manages the timing of the simulation.
the timing of the simulation. Each of these layers has a reference to the layer
to the layer below it

### Simulation

The simulation is based on a tick principle. Thus, a tick counter is constantly
counter is constantly incremented and a simulation step is executed at each increment. One
such a simulation step corresponds e.g. to the movement of a vehicle,
picking up a delivery, etc. Specifically, each time from top to
the tick(long) method of each layer is called, which will then
which then all move to the next state. Each simulation step that is executed generates an event. A list of all generated
events is returned at the end of the tick methods
at the end. These events are used at the end of the simulation by the rater interface (see H8) to evaluate the
simulation with respect to certain criteria.
evaluate the simulation with respect to certain criteria.

The individual orders are represented in the simulation by the ConfirmedOrder class. This has the
following properties:
| Attribute Name | Type | Description |
| ------------------ | ------------- | ------------------------------------------------------------- |
| location | Location | The coordinates of the destination |
| orderID | int | The ID of the order |
| tickInterval | TickInterval | The time period in which the delivery should be made. |
| foodList | List<String>  | The actual order; a list of dishes there are to be delivered. |
| weight | double | The weight of the order. |

The OrderGenerator interface (see H7) is responsible for generating these orders.

Which problems are simulated is described by implementations of the interface ProblemArchetype.
Classes implementing this interface consist of four components each, which can be retrieved via the methods declared in the interface.
interface can be retrieved.
- Vehicle Manager: Describes the structure of the underlying region and the available vehicles.
- Order Generator Factory: Describes the orders received by the delivery service (see H7).
- Rater Factory Map: Describes which rater is used to evaluate each criterion.
- Simulation Length: Describes how long the simulation should last, i.e. how long orders are delivered.
  Above the ProblemArchetype interface there is also the ProblemGroup class, which combines several of these problems
  and specifies which evaluation criteria are to be evaluated and how.
  Finally, there is the interface Runner (see H10), which contains an instance of the class ProblemGroup, a
  SimulationConfig, the number of simulations to run, and a deliveryServiceFactory.
  This is responsible for each object of ProblemArchetype from the ProblemGroup to run a simulation based on the created
  DeliveryService.
  based on the created DeliveryService, to run it as many times as specified, and at the end to get the average score
  for each
  and to calculate and return the average score for each evaluation criterion.

