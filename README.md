# Lieferservice 
Lieferservice Projekt für Funktionale und objektorientierte Programmierkonzepte WiSe 2022/23 

## Project Description:

You were asked by a well-known food delivery service to develop a program that allows you to simulate and
to simulate the process of a delivery service and to evaluate it at the end. Complete the following tasks in the template
the following tasks to complete the template that we provide to you. The template is divided into three
parts:
- Application: From here, the actual simulation is started. Here you will implement the task H10.
ting.
- Domain: This is where the implementation of the basic problem is located. Except for task H10 and H11 you will
implement your solutions here.
- Infrastructure: This is where the communication with the "outside world" is located, i.e. the implementation of the GUI and IO
operations. Here you will implement task H11.
The domain is built up in layers. The lowest layer is represented by
the interfaces region (see H2) and describes by means of a gra-
of nodes (see H3) and edges (see H4) the structure of the delivery area.
area. The next layer is implemented by the interface VehicleManager (see H6).
and is responsible for managing the different vehicles (see H5) of the delivery service.
delivery service. On the third layer, incoming orders are accepted, managed and
orders are accepted, managed and assigned to the individual vehicles. This
is represented by the interface DeliveryService (see H9). On the last
layer is the Simulation interface, which manages the timing of the simulation.
the timing of the simulation. Each of these layers has a reference to the layer
to the layer below it.

Translated with www.DeepL.com/Translator (free version)
