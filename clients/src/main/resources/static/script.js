// Event listener for menu navigation


// Function to fetch and render "Me" details
function fetchAndRenderMeDetails() {
  fetch('me')
    .then(response => response.json())
    .then(data => {
      const contentContainer = document.getElementById('content');
      contentContainer.innerHTML = `<h2>Me Page</h2><p>${data.me}</p>`;
    })
    .catch(error => {
      console.error('Error fetching "Me" details:', error);
    });
}

// Function to fetch and render "Peers" details
function fetchAndRenderPeers() {
  fetch('peers')
    .then(response => response.json())
    .then(data => {
      const contentContainer = document.getElementById('content');
      const peers = data.peers;
      let peersHTML = '<h2>Peers Page</h2>';
      if (peers.length > 0) {
        peersHTML += '<ul>';
        peers.forEach(peer => {
          peersHTML += `<li>${peer}</li>`;
        });
        peersHTML += '</ul>';
      } else {
        peersHTML += '<p>No peers found.</p>';
      }
      contentContainer.innerHTML = peersHTML;
    })
    .catch(error => {
      console.error('Error fetching "Peers" details:', error);
    });
}
// Function to fetch and render "ious" details in proposal
function fetchAndRenderProposals() {
  fetch('ious')
    .then(response => response.json())
    .then(data => {
      const contentContainer = document.getElementById('content');
      const proposals = data;
      let proposalsHTML = '<h2>Proposals Page</h2>';
      if (proposals.length > 0) {
        proposalsHTML += '<ul>';
        proposals.forEach(proposal => {
          const { amount, unitPrice, rate, buyer, seller, proposer, proposee, linearId } = proposal.state.data;
          proposalsHTML += `
            <li>
              <strong>Amount:</strong> ${amount}<br>
              <strong>Unit Price:</strong> ${unitPrice}<br>
              <strong>Rate:</strong> ${rate}<br>
              <strong>Buyer:</strong> ${buyer}<br>
              <strong>Seller:</strong> ${seller}<br>
              <strong>Proposer:</strong> ${proposer}<br>
              <strong>Proposee:</strong> ${proposee}<br>
              <strong>Linear ID:</strong> ${linearId.id}<br>
            </li>
          `;
        });
        proposalsHTML += '</ul>';
      } else {
        proposalsHTML += '<p>No proposals found.</p>';
      }
      contentContainer.innerHTML = proposalsHTML;
    })
    .catch(error => {
      console.error('Error fetching "Proposals" details:', error);
    });
}
// Function to fetch ans render "transaction-iou" datiles in Transactions
function fetchAndRenderTransactions() {
  fetch('transaction-iou')
    .then(response => response.json())
    .then(data => {
      const contentContainer = document.getElementById('content');
      const proposals = data;
      let proposalsHTML = '<h2>Proposals Page</h2>';
      if (proposals.length > 0) {
        proposalsHTML += '<ul>';
        proposals.forEach(proposal => {
          const { amount, unitPrice, rate, buyer, seller, proposer, proposee, linearId } = proposal.state.data;
          proposalsHTML += `
            <li>
              <strong>Amount:</strong> ${amount}<br>
              <strong>Unit Price:</strong> ${unitPrice}<br>
              <strong>Rate:</strong> ${rate}<br>
              <strong>Buyer:</strong> ${buyer}<br>
              <strong>Seller:</strong> ${seller}<br>
              <strong>Proposer:</strong> ${proposer}<br>
              <strong>Proposee:</strong> ${proposee}<br>
              <strong>Linear ID:</strong> ${linearId.id}<br>
            </li>
          `;
        });
        proposalsHTML += '</ul>';
      } else {
        proposalsHTML += '<p>No Transactions found.</p>';
      }
      contentContainer.innerHTML = proposalsHTML;
    })
    .catch(error => {
      console.error('Error fetching "Proposals" details:', error);
    });
}
// post form
function handleProposeTransaction() {
//  const formContainer = document.getElementById('transactionFormContainer');
  const iouValueInput = document.getElementById('iouValueInput');
  const rateInput = document.getElementById('rateInput');
  const unitPriceInput = document.getElementById('unitPriceInput');
  const partySelect = document.getElementById('partySelect');
  const submitButton = document.getElementById('submitButton');

//  transactionFormContainer.style.display = 'block';
  // Fetch and render the list of peers
  fetch('/peers')
    .then(response => response.json())
    .then(data => {
      data.peers.forEach(peer => {
        const option = document.createElement('option');
        option.value = peer;
        option.textContent = peer;
        partySelect.appendChild(option);
      });
    })
    .catch(error => {
      console.error('Error fetching peers:', error);
    });

  // Event listener for the "Propose Transaction" button

  // Event listener for the form submission
  submitButton.addEventListener('click', () => {
    const iouValue = iouValueInput.value;
    const rate = rateInput.value;
    const unitPrice = unitPriceInput.value;
    const partyName = partySelect.value;

    console.log(partyName)
    // Send the post request
    const url = `create-iou?isBuyer=true&iouValue=${iouValue}&rate=${rate}&unitPrice=${unitPrice}&partyName=${partyName}`;
    fetch(url, { method: 'POST' })
      .then(response => response.text())
      .then(data => {
        // Show response in a pop-up box
        window.alert(data);
      })
      .catch(error => {
        console.error('Error submitting transaction:', error);
      });

    // Clear the form inputs and hide the form
    iouValueInput.value = '';
    rateInput.value = '';
    unitPriceInput.value = '';
    formContainer.style.display = 'none';
  });
}
// toggle proposal content
function toggleProposal() {
  const container = document.getElementById('transactionFormContainer')
  if (container.style.display === "none") {
    container.style.display = "block";
  } else {
    container.style.display = "none";
  }
}


// Function to render content for the selected menu option
function renderContent(content) {
  const contentContainer = document.getElementById('content');
  contentContainer.innerHTML = content;
}

document.addEventListener('DOMContentLoaded', () => {
  const homeButton = document.getElementById('home-button');
  const proposeButton = document.getElementById('propose-button');
  const modifyButton = document.getElementById('modify-button');
  const submitButton = document.getElementById('submit-button');
  const meButton = document.getElementById('me-button');
  const peersButton = document.getElementById('peers-button');
  const proposalsButton = document.getElementById('proposals-button');
  const finalizeButton = document.getElementById('finalize-button');





  homeButton.addEventListener('click', (event) => {
    event.preventDefault();
    renderContent('<h2>Home Page</h2>');
  });

  proposeButton.addEventListener('click', (event) => {
    event.preventDefault();
//    formContainer.style.display = 'block';
//    try {
//        handleProposeTransaction();
//    } catch (e) {
//        console.error(e && e.message || 'error')
//    }


//    renderContent('<h2>Proposal text Page</h2>');

  });

  modifyButton.addEventListener('click', (event) => {
    event.preventDefault();
    renderContent('<h2>Modify 2 Transaction Page</h2>');
  });

  submitButton.addEventListener('click', (event) => {
    event.preventDefault();
    renderContent('<h2>Submit Transaction Page</h2>');
  });

  meButton.addEventListener('click', (event) => {
    event.preventDefault();
    fetchAndRenderMeDetails();
  });

  peersButton.addEventListener('click', (event) => {
    event.preventDefault();
    fetchAndRenderPeers();
  });

  proposalsButton.addEventListener('click', (event) => {
    event.preventDefault();
    fetchAndRenderProposals();
  });

  finalizeButton.addEventListener('click', (event) => {
    event.preventDefault();
    fetchAndRenderTransactions();
  });
});
