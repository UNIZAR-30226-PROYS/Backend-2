### Backend API Documentation

#### **/users**

##### /{nick}/signup
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

##### /{nick}/login
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
