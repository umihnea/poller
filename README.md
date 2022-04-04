## poller

Prototype for an (Internet) service availability monitor. Composed of a Vert.x Java backend service spawning a pool of
polling verticles and a rudimentary React front-end (with Redux as state manager).

### Requirements

1. Node.js v14.19.1, `yarn` (v1.22.18) package manager
2. Java 17 (I used OpenJDK 17.0.2)
3. MySQL 8.0.28

### Installation

1. Clone this git repository.
2. Install front-end dependencies.

```bash
$ cd frontend && yarn
```

3. The server needs a connection to a MySQL database. The credentials for this connections are to be provided through
   the `.env` file.

```bash
$ cd backend/src/main/resources
$ cp .example.env .env
```

4. Edit the file with your editor of choice and provide the database credentials.
5. The MySQL database itself can be initialized by running the contents of the `backend/initial.sql` file.
6. Install the backend requirements.

```bash
$ cd backend
$ ./mvnw clean install
$ ./mvnw compile
```

### Usage

1. Run the Vert.x server at `localhost:8080`.

```bash
$ ./mvnw exec:java
```

2. Launch the React server at `localhost:3000`.

```bash
$ yarn start
```
