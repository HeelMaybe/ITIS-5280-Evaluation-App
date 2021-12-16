
# EvaluationApp
This API is the project #10 for the class **ITIS 5280** at UNCC. It consists of a server to support a Mobile Application for evaluating posters. 


## Technologies

In order to develop this app, we used:
- An Android Application as the **front-end**
	- For Android 10 (API level 29)
	- Material Design as the style guideline
	- Auth0 for authenticating
- NodeJS **back-end**
	- Auth0 for authenticating
	- MongoDB as the database
	- Twilio SMS 

## Data Design
The main collections in the database are the following:
|Collection Name |Description                    		 |Properties				   |
|----------------|---------------------------------------|-----------------------------|
|`Posters`		 |Information of the posters       		 | `Title`, `Participants`, `NFC Tag`, `current score`, `Evaluation Details`|
|`Examiners`		 |Information of the examiners       		 | `name`,  `email `, `creation date`, `status`|


## Routes
The routes of the API endpoints are the following:
- GET `/api/posters`: get all the posters from the database
- POST `/api/poster`: post evaluation for a single poster

## External Resources
- Video explaining the app: https://youtu.be/EMjRUo0D9HM
- Update Video explaining the changes to the app(12/15/2021): https://youtu.be/13CTIR8ZxqU 

## Support

We might not be supporting future releases, buy anyone can still get us a cup of coffee!
