"use strict";

const express = require("express");
const bodyParser = require("body-parser");

const Datastore = require("@google-cloud/datastore");
const datastore = Datastore();

const app = express();
app.enable("trust proxy");
app.use(bodyParser.json());

const RequestType = {
  CREATE: 0,
  DELETE: 1,
  READ: 2
};

function createUserRecord(user) {
  const userKey = datastore.key(["User", user.id]);
  const entity = {
    key: userKey,
    data: user
  };
  return datastore.save(entity)
    .catch((err) => {
      console.error("Error: " + err);
    });
}

function deleteUserRecord(userID) {
  const userKey = datastore.key(["User", userID]);
  return datastore.delete(userKey)
    .catch((err) => {
      console.error("Error: " + err);
    });
}

function getUserRecord(userID) {
  const query = datastore.createQuery("User")
    .filter("id", userID);
  return datastore.runQuery(query)
    .then((results) => {
      return results[0][0];
    })
    .catch((err) => {
      console.error("Error: " + err);
    });
}

function createGroupRecord(group) {
  const groupKey = datastore.key(["Group", group.id]);
  const entity = {
    key: groupKey,
    data: group
  };
  return datastore.save(entity)
    .catch((err) => {
      console.error("Error: " + err);
    });
}

function deleteGroupRecord(groupID) {
  const groupKey = datastore.key(["Group", groupID]);
  return datastore.delete(groupKey)
    .catch((err) => {
      console.error("Error: " + err);
    });
}

function getGroupRecord(groupID) {
  const query = datastore.createQuery("Group")
    .filter("id", groupID);
  return datastore.runQuery(query)
    .then((results) => {
      return results[0][0];
    })
    .catch((err) => {
      console.error("Error: " + err);
    });
}

app.post("/users", (req, res) => {
  const data = req.body;
  if (data.requestType === RequestType.CREATE) {
    createUserRecord(data.content)
      .then(() => {
        res.json({content: "Success: Created User"}).end();
      });
  } else if (data.requestType === RequestType.DELETE) {
    deleteUserRecord(data.content)
      .then(() => {
        res.json({content: "Success: Deleted User"}).end();
      });
  } else if (data.requestType === RequestType.READ) {
    getUserRecord(data.content)
      .then((user) => {
        res.json(user).end();
      });
  }
});

app.post("/groups", (req, res) => {
  const data = req.body;
  if (data.requestType === RequestType.CREATE) {
    createGroupRecord(data.content)
      .then(() => {
        res.json({content: "Success: Created Group"}).end();
      });
  } else if (data.requestType === RequestType.DELETE) {
    deleteGroupRecord(data.content)
      .then(() => {
        res.json({content: "Success: Deleted Group"}).end();
      });
  } else if (data.requestType === RequestType.READ) {
    getGroupRecord(data.content)
      .then((user) => {
        res.json(user).end();
      });
  }
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log("App listening on port " + PORT);
});

module.exports = app;
