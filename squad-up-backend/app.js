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

function createUserRecord(data) {
  const dataKey = datastore.key(["Kind", data.key]);
  const entity = {
    key: dataKey,
    data: data
  };
  return datastore.save(entity)
    .catch((err) => {
      console.error("Error: " + err);
    });
}

function deleteUserRecord(key) {
  const dataKey = datastore.key(["Kind", key]);
  return datastore.delete(dataKey)
    .catch((err) => {
      console.error("Error: " + err);
    });
}

function getUserRecord(key) {
  const query = datastore.createQuery("Kind")
    .filter("key", key);
  return datastore.runQuery(query)
    .then((data) => {
      return data[0][0];
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
        res.json({content: "Success"}).end();
      });
  } else if (data.requestType === RequestType.DELETE) {
    deleteUserRecord(data.content)
      .then(() => {
        res.json({content: "Success"}).end();
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
        res.json({content: "Success"}).end();
      });
  } else if (data.requestType === RequestType.DELETE) {
    deleteGroupRecord(data.content)
      .then(() => {
        res.json({content: "Success"}).end();
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
