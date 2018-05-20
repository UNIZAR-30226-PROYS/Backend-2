# Backend API Documentation

## Table of contents

 - [GET /users/{nick}?token={token}](#get-usersnicktokentoken)
 - [DELETE /users/{nick}?token={TOKEN}](#delete-usersnicktokentoken)
 - [PUT /users/{nick}?token={token}](#put-usersnicktokentoken)
 - [POST /users/{nick}/signup](#post-usersnicksignup)
 - [POST /users/{nick}/login](#post-usersnicklogin)
 - [DELETE /users/{nick}/login?token={TOKEN}](#delete-usersnicklogintokentoken)
 - [POST /users/{nick}/verify](#post-usersnickverify)

 - [GET /songs/{id}](#get-songsid)

## Requests

### **/users**

#### GET /users/{nick}?token={token}
This requests gets the data of a user with the nick {nick}.

Accepts the following parameters in an HTTP GET request:
  - nick => Nick of this user.
  - token (Optional) => Login token. If this token matches {nick}'s token, sensible information (mail, country, birth_date and register_date) will be returned.

RestAPI will answer with this JSON response:
```json
  {
    "profile" : {
      "id": "{ID}",
      "nick": "{NICK}",
      "user": "{USER}",
      "bio": "{BIO}",
      "verified": "true/false",

      "mail_visible" : "true/false",
      "mail" : "{MAIL}",
      "country" : "{COUNTRY}",
      "birth_date" : "{BIRTH_DATE}",
      "register_date" : "{REGISTER_DATE}"
    },
    "error" : "true/false"
  }
```

If *"error"* is true, the profile will be empty, this means, all fields will be defined **but** its value is unspecified.

Also, if *"mail_visible"* is false, all private fields will be defined but its value is also unspecified.

*"country"* contains the 2 character ISO Code specified in ISO_3166-1

Types:

| Parameter | Type |
| :---: |:---|
| *"id"* | Integer |
| *"nick"* | String |
| *"user"* | String |
| *"bio"* | String |
| *"mail_visible"* | Boolean |
| *"mail"* | String |
| *"country"* | String |
| *"birth_date"* | Date |
| *"register_date"* | Long |
| *"error"* | Boolean |

#### DELETE /users/{nick}?token={TOKEN}

This requests deletes a user with the nick {nick}. That means the system will no longer know this user even existed.

Accepts the following parameters in an HTTP DELETE encoded request (application/x-www-form-urlencoded):
  - nick => Nick of this user.
  - token => Login token. If this token matches {nick}'s token, current session will be invalidated.

RestAPI will answer with this JSON response:
```json
  {
    "error" : "{ERROR_CODE}"
  }
```

Error codes are specified as follows:

| {ERROR_CODE} | Description |
| :---: |:---|
| ok | User has been logged out successfully |
| invalidArgs | Token parameter is null or empty. |
| invalidToken | Given {TOKEN} doesn't match {nick}'s token. |
| unknownUser | No user with that nick exists in the Database. |
| unknownError | An unknown error happened when trying to delete user session |

Types:

| Parameter | Type |
| :---: |:---|
| *"error"* | String |

#### PUT /users/{nick}?token={token}

This requests updates the data associated to a user with the nick {nick} in the database.

Accepts the following parameters in an HTTP PUT encoded request (**application/json**):
  - nick => User's nick.
  - token => User's session token.
  - *request_body* =>
    ```json
      {
        "updates" : {
          "update_key0" : "value",
          "update_key1" : "value",

          "update_keyN" : "value"
        }
      }
    ```

Request body update keys and its values **MUST** be one of the following:

| *update_key* | Type | Example |
| ---: | :---: | :--- |
| username | String | `"username" : "Newname"` |
| mail | String | `"mail" : "NewMail"` |
| bio | String | `"bio" : "NewBio"` |
| birth_date | Long | `"birth_date" : -1` |
| pass | JSONObject | `{"pass0" : "NewPass", "pass1" : "NewPass(again)", "old_pass" : "OldPass"}` |

RestAPI will answer with this JSON response:
```json
  {
    "error" : "{ERROR_CODE}"
  }
```
or this one
```json
  {
    "error" : {
      "update_key0" : "{ERROR_CODE}",
      "update_key1" : "{ERROR_CODE}",

      "update_keyN" : "{ERROR_CODE}"
    }
  }
```

If "error" is a String, no update has been made. "error" will be one of:

| {ERROR_CODE} | Description |
| :---: |:---|
| invalidArgs | At least one of the given arguments or request body are null or empty. |
| noUpdate | Request body doesn't have "updates" JSON Object
| unknownUser | No user with that nick exists in the Database. |
| invalidToken | Given {TOKEN} doesn't match {nick}'s token. |
| closedSession | This user had already closed his session. |


If "error" is a JSONObject, each key matches exactly the same keys provided for updating and its value shows if the update has been made or not and why. Each key is associated one of this values

| {ERROR_CODE} | Description |
| :---: |:---|
| ok | Update has been made successfully |
| passError | **ONLY for pass**. pass0 and pass1 don't match or old_pass is not user's password. |
| invalidValue | Wrong type. This means String is provided when expecting Integer or vice versa |

Types:

| Parameter | Type |
| :---: |:---|
| *"error"* | String or JSONObject |

#### POST /users/{nick}/signup
This requests registers a new user in the database with the nick {nick}. That nick **MUST** be unique.

Accepts the following parameters in an HTTP POST encoded request (application/x-www-form-urlencoded):
  - user => User's full name (**NOT UNIQUE**).
  - mail => Email of this user.
  - pass0 => Password of the new user.
  - pass1 => Same password as pass0
  - birth => Birth date of this user as specified by the epoch standard. It means, the number of *milliseconds* passed since 1st January 1970 (negative number means the date is before).
  - bio (Optional) => Biography of this user.

RestAPI will answer with this JSON response:
```json
  {
    "token" : "{TOKEN}",
    "error" : "{ERROR_CODE}"
  }
```

If {ERROR_CODE} is "ok", {TOKEN} will have a sequence of 16 characters next requests might need to provide authentication.

Other error codes are specified as follows:

| {ERROR_CODE} | Description |
| :---: |:---|
| invalidArgs | At least one of the given arguments are null, empty or NaN when expecting a number. Also, this error is produced when nick is not between 3 and 32 characters (both inclusive) |
| wrongMail | mail is not valid. |
| notEqualPass | pass0 and pass1 don't match. |
| userExists | Another user with that nick exists in the Database. |
| unknownError | An unknown error happened when trying to push the new user to the database |

Types:

| Parameter | Type |
| :---: |:---|
| *"token"* | String |
| *"error"* | String |

#### POST /users/{nick}/login
This requests creates a new session for a user with the nick {nick}. If that user had another opened session before, it will be removed (aka "closed").

Accepts the following parameters in an HTTP POST encoded request (application/x-www-form-urlencoded):
  - pass => Password of this user.

RestAPI will answer with this JSON response:
```json
  {
    "user" : "{NICK}",
    "token" : "{TOKEN}",
    "error" : "{ERROR_CODE}"
  }
```

{NICK} will **always** contain the same nick provided in the URL.

If {ERROR_CODE} is "ok", {TOKEN} will have a sequence of 16 characters next requests might need to provide authentication.

Other error codes are specified as follows:

| {ERROR_CODE} | Description |
| :---: |:---|
| invalidArgs | Provided pass is null or empty. |
| passError | Given pass is not valid for this user. |
| userNotExists | There is no user with that nick in the Database. |

Types:

| Parameter | Type |
| :---: |:---|
| *"token"* | String |
| *"error"* | String |
| *"user"* | String |

#### DELETE /users/{nick}/login?token={TOKEN}

This requests logouts a user with the nick {nick}. That means previous token will not be valid.

Accepts the following parameters in an HTTP DELETE encoded request (application/x-www-form-urlencoded):
  - nick => Nick of this user.
  - token => Login token. If this token matches {nick}'s token, current session will be invalidated.

RestAPI will answer with this JSON response:
```json
  {
    "error" : "{ERROR_CODE}"
  }
```

Error codes are specified as follows:

| {ERROR_CODE} | Description |
| :---: |:---|
| ok | User has been logged out successfully |
| invalidArgs | Token parameter is null or empty. |
| invalidToken | Given {TOKEN} doesn't match {nick}'s token. |
| closedSession | This user had already closed his session. |
| unknownUser | No user with that nick exists in the Database. |
| unknownError | An unknown error happened when trying to delete user session |

Types:

| Parameter | Type |
| :---: |:---|
| *"error"* | String |

#### POST /users/{nick}/verify

This requests verifies/unverifies a user with the nick {nick}.

Accepts the following parameters in an HTTP POST encoded request (application/x-www-form-urlencoded):
  - self => Nick of the admin user
  - token => Admin login session token. If this token matches {self}'s token, {nick} will be verified / unverified.
  - verify => True to verify, false to unverify.

RestAPI will answer with this JSON response:
```json
  {
    "error" : "{ERROR_CODE}"
  }
```

Error codes are specified as follows:

| {ERROR_CODE} | Description |
| :---: |:---|
| ok | User has been verified/unverified successfully |
| invalidArgs | Token, user or admin parameters are null or empty. |
| invalidToken | Given {TOKEN} doesn't match {nick}'s token. |
| closedSession | This user had already closed his session. |
| unknownUser | No user with that nick exists in the Database. |
| unknownError | An unknown error happened when trying to update user data. |
| noPermission | {self} is not an admin and therefore doesn't have permission to verify/unverify any account. |
| cannotUnverify | **ONLY if "verify" = false**. Cannot unverify an unverified account. |

Types:

| Parameter | Type |
| :---: |:---|
| *"error"* | String |

#### GET /users/{nick}/feed?nFollow={amount}&nSongs={amount}&nRepr={amount}

This requests returns the feed of user with the nick {nick}. The feed will contain the last users {nick} followed, the last songs {nick} reproduced and the last songs {nick} uploaded.

Accepts the following parameters in an HTTP GET encoded request (application/x-www-form-urlencoded):
  - nick => User to retrieve feed data.
  - nFollow => Max amount of user follows to retrieve (defaults to 15). 
  - nSongs => Max amount of user songs uploads to retrieve (defaults to 15).
  - nRepr => Max amount of user songs reproductions to retrieve (defaults to 15).

RestAPI will answer with this JSON response:
```json
  {
    "follow" : "{ARRAY_OF_DATA}",
    "songs" : "{ARRAY_OF_DATA}",
    "repr" : "{ARRAY_OF_DATA}",
    "error" : "{ERROR_CODE}"
  }
```
where *{ARRAY_OF_DATA}* is
```json
  [
    {
      "id" : "{USER_OR_SONG_ID}",
      "time" : "{EPOCH_TIME}"
    }
  ]
```

Error codes are specified as follows:

| {ERROR_CODE} | Description |
| :---: |:---|
| ok | Feed is OK and has been retrieved successfully |
| invalidArgs | *nFollow*, *nSongs* or *nRepr* is lower or equal to zero. |
| unknownUser | No user with that nick exists in the Database. |

Types:

| Parameter | Type |
| :---: |:---|
| *"error"* | String |
| *"time"* | Long |
| *"id"* | Long |
| *"follow"* | JSONArray |
| *"songs"* | JSONArray |
| *"repr"* | JSONArray |

### **/songs**

#### GET /songs/{id}
This requests gets the data of a song with the id {id}.

Accepts the following parameters in an HTTP GET request:
  - id => Song id.

RestAPI will answer with this JSON response:
```json
  {
    "profile" : {
      "id": "{ID}",
      "user_id": "{AUTHOR}",
      "title": "{TITLE}",
      "country": "{COUNTRY}",
      "upload_time" : "{UPLOAD_TIME}",
    },
    "error" : "{ERROR_CODE}"
  }
```

If *"error"* is not "ok", the song will be empty, this means, all fields will be defined **but** its value is unspecified.

Other {ERROR_CODE}s are:

| {ERROR_CODE} | Description |
| :---: |:---|
| invalidArgs | *id* is lower or equal to zero. |
| unknownSong | No song is registered with that id |

Types:

| Parameter | Type |
| :---: |:---|
| *"id"* | Long |
| *"user_id"* | Long |
| *"title"* | String |
| *"country"* | String |
| *"upload_time"* | Long |
| *"error"* | String |

#### GET /songs/{id}/recommend?n={amount}
This requests returns a list of a maximum of n songs recommended due to its similarity with {id}.

Accepts the following parameters in an HTTP GET request:
  - id => Song id.
  - n => Number of songs to retrieve

RestAPI will answer with this JSON response:
```json
  {
    "amount":"{AMOUNT}",
    "songs": "{ARRAY_OF_SONG_IDS}",
    "error" : "{ERROR_CODE}"
  }
```

Other {ERROR_CODE}s are:

| {ERROR_CODE} | Description |
| :---: |:---|
| invalidArgs | *id* is lower or equal to zero. |
| unknownSong | No song is registered with that id |

Types:

| Parameter | Type |
| :---: |:---|
| *"songs"* | JSONArray of Integer |
| *"amount"* | Integer |
| *"error"* | String |
