# Backend API Documentation

## Table of contents

 - [GET /users/{nick}?token={token}](#get-usersnicktokentoken)
 - [DELETE /users/{nick}?token={TOKEN}](#delete-usersnicktokentoken)
 - [POST /users/{nick}/signup](#post-usersnicksignup)
 - [POST /users/{nick}/login](#post-usersnicklogin)
 - [DELETE /users/{nick}/login?token={TOKEN}](#delete-usersnicklogintokentoken)

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

#### DELETE /users/{nick}?token={TOKEN}
