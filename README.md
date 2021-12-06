
# EvaluationApp
This API is the project #10 for the class **ITIS 5280** at UNCC. It consists of a server to support a Mobile Application for authenticating. 


## Technologies

In order to develop this app, we used:
- An Android Application as the **front-end**
	- For Android 10 (API level 29)
	- Material Design as the style guideline
	- Auth0 for authenticating
- NodeJS **back-end**
	- JWT Tokens for security tokens
	- MongoDB as the database

## Data Design
The main collections in the database are the following:
|Collection Name |Description                    		 |Properties				   |
|----------------|---------------------------------------|-----------------------------|
|`users`		 |Information of the users       		 | `name`, `age`, `weight`, `email (unique key)`, `salt`, `hash`|

## Routes
The routes of the API endpoints are the following:
- GET `/api/posters`: get all the posters from the database
- POST `/api/poster`: post evaluation for a single poster


## Support

We might not be supporting future releases, buy anyone can still get us a cup of coffee!
