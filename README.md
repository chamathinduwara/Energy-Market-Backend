# Energy Market Backend

Power your energy market operations with the Energy Market Backend – a Java 8-based solution built on Corda for efficient and secure energy trading. This backend facilitates seamless proposal management, transaction execution, and data integrity, ensuring a reliable and transparent energy marketplace.

## Table of Contents
- [Description](#description)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Negotiation States](#negotiation-states)
- [Flows](#flows)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Description
The Energy Market Backend is a robust Java application built on Corda, designed to streamline energy trading processes. Leverage the power of distributed ledger technology to manage proposals, execute transactions securely, and maintain an auditable record of energy market activities.

## Features
- Create, update, and finalize energy trading proposals.
- Execute secure and tamper-proof transactions using Corda's blockchain technology.
- Maintain a transparent and immutable ledger of energy market activities.
- Ensure data privacy and integrity through Corda's advanced cryptographic techniques.

## Prerequisites
Before you begin, ensure you have the following prerequisites:
1. Java Development Kit (JDK) 8.
2. Corda Network environment configured.

## Installation
Clone the repository:
  ```bash`
  ```git clone https://github.com/yourusername/energy-market-backend.git```

## Negotiation States

The Energy Market Backend models the negotiation process using the following Corda states:

* `ProposalState`: Created by sellers to propose energy trades to specific buyers.
* `ModifyState`: Buyers can modify proposed trades or reject them, leading to this state.
* `TradeState`: Represents a finalized energy trade accepted by both parties.
* `KillState`: A state for rejected proposals or trades that are terminated.


## Flows
The energy market process is facilitated through the following Corda flows:

* `ProposalFlow`: Initiate a proposal by creating a ProposalState.
* `ModificationFlow`: Modify a proposal using an existing ProposalState, resulting in a ModifyState.
* `AcceptanceFlow`: Accept a proposal by creating a TradeState from a modified proposal
* `KillProposalFlow`: Reject a proposal and create a KillState.
* `KillModificationFlow`: Reject a modified proposal and create a KillState.

These flows orchestrate the negotiation and transaction process within the energy market.

### Running the nodes:

Open a terminal and go to the project root directory and type: (to deploy the nodes)
```
./gradlew clean deployNodes
```
Then type: (to run the nodes)
```
./build/nodes/runnodes
```

You can interact with this CorDapp via the nodes shells.

First, go the the shell of PartyA, and propose a deal with yourself as buyer and a value of 10 to PartyB:

    flow start ProposalFlow$Initiator isBuyer: true, amount: 10, unitPrice: 10, counterparty: PartyB

you can now look at the proposals in the PartyA's vault:

    run vaultQuery contractStateType: ProposalState

If You note down the state's `linearId.id`, you can now modify the proposal from the shell of PartyB by running:

    flow start ModificationFlow$Initiator proposalId: <YOUR-NEWLY-GENERATED-PROPOSAL-ID>, newAmount: 8, newUnitPrice: 8

Also, You can Reject the Proposal as PartyA or PartyB by running:

    flow start KillProposalFlow$Initiator proposalId: <YOUR-NEWLY-GENERATED-PROPOSAL-ID>
    
Also, You can Reject the Modified Proposal as PartyA or PartyB by running:

    flow start KillModificationFlow$Initiator proposalId: <YOUR-NEWLY-GENERATED-PROPOSAL-ID>

Finally, let's have PartyA accept the proposal:

    flow start AcceptanceFlow$Initiator proposalId: <YOUR-NEWLY-GENERATED-PROPOSAL-ID>

We can now see the accepted trade in our vault with the new value by running the command:

    run vaultQuery contractStateType: TradeState

## Contributing
Contributions are welcome! To enhance the Energy Market Backend:

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature-name`
3. Implement your changes and commit: `git commit -m 'Add feature'`
4. Push to your branch: `git push origin feature-name`
5. Create a pull request.

## License
This project is open source and available under the MIT License.

## Contact
* For inquiries or support, reach out to us at csschamathinduwara@gmail.com
* Connect with us on GitHub: `chamathinduwara`

