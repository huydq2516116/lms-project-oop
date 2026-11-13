# lms-project-oop
## Use Cases:
After determining the actors, the second step in use case analysis is to determine the tasks that each actor will need to do with the system. Each task is called a use case because it represents one particular way the system will be used.

**In other words, only those use cases are listed that actors will need to do when they are using the system to solve the customer’s problem.** 

### Borrower:
* ❏ Search for items by title.
* ❏ ... by author.
* ❏ ... by subject.
* ❏ Place a book on hold if it is on loan to somebody else.
* ❏ Check  the  borrower’s  personal  information  and  list  of  books  currently
borrowed.

### Checkout Clerk:
* ❏ All the Borrower use cases, plus
* ❏ Check out an item for a borrower.
* ❏ Check in an item that has been returned.
* ❏ Renew an item.
* ❏ Record that a fine has been paid.
* ❏ Add a new borrower.
* ❏ Update a borrower’s personal information (address, telephone number etc.).

### Librarian:
* ❏ All of the Borrower and Checkout Clerk use cases, plus
* ❏ Add a new item to the collection.
* ❏ Delete an item from the collection.
* ❏ Change the information the system has recorded about an item.

### Administrator:
* ❏ Add Clerk.
* ❏ Add Librarian.
* ❏ View Issued Books History.
* ❏ View All Books in Library.

## lms plan
### lms.database: 
+ static DatabaseOperations: only class that contains all the CRUD method that connects to the database

### lms.object:
+ Book: Manages its own inventory state. When a book is checked out, lost, or removed, it updates its status and synchronizes the change to the database.
+ HoldRequest: Tracks active reservation queues. It can verify expiration windows, cancel itself, or transition into a loan when fulfilled.
+ Loan: Handles circulation math. It computes late fines based on due dates, handles renewal requests, and coordinates closing out a transaction when a book is returned.
+ Only use methods from DatabaseOperations

### lms.person:
+ Borrower: Executes cardholder actions. A borrower can place hold requests on books, initiate checkouts, check their active loan list, and pay off accumulated fines.
+ Staff (Librarian, Clerk): Executes operational privileges. Staff members can register new library users, add/remove books from the catalog, process returns, handle fine overrides.

### lms.screen:
+ LoginScreen: Authenticates user credentials, determines user roles, and routes the session to the correct dashboard.
+ BorrowerScreen: Renders search results, active loans, and hold lists. It displays data retrieved from Borrower/Book objects and triggers instance methods when users click buttons (e.g., clicking "Hold" calls borrower.placeHold(...)).
+ ClerkScreen / LibrarianScreen / AdministratorScreen: Render management dashboards. They gather form inputs from staff and pass them to Staff methods to handle inventory, user registration, and administrative workflows without touching database code directly.
+ AdministratorScreen / LoginScreen just use methods from DatabaseOperations
