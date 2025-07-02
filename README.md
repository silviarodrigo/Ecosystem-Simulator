# Ecosystem Simulator: Wolves & Sheep

This project is a Java-based ecosystem simulation developed in Eclipse. It models the life and interaction between two species within a customizable environment: carnivorous **wolves** and herbivorous **sheep**. The simulation progresses in time steps, allowing the user to monitor the delicate balance of a virtual ecosystem.

This project was created as a practical assignment for a second-year programming course, focusing on Object-Oriented Programming (OOP) principles.

## Core Features

-   **Dynamic Population**: Watch the populations of wolves and sheep fluctuate based on birth, death, and predation.
-   **Complex Animal States**: Animals are not just alive or dead. They transition through various states that affect their behavior:
    -   `NORMAL`: Default state.
    -   `MATE`: Actively seeking a partner to reproduce.
    -   `HUNGER`: Actively seeking food.
    -   `DANGER`: A sheep is aware of a nearby wolf.
    -   `DEAD`: The animal has been removed from the simulation.
-   **Multi-Region World**: The ecosystem is divided into distinct regions, each with its own characteristics.
-   **Customizable Regions**: Users can create new regions and define key parameters, such as the initial amount of vegetation (food for sheep) and the growth factor of it.
-   **Detailed Monitoring**: At each time step, the simulation provides a detailed report on:
    -   The total number of animals.
    -   The count of animals in each state.
    -   The population distribution across a specific region.
-   **Time-Based Progression**: The simulation runs in discrete time steps, allowing for clear observation of cause and effect.
