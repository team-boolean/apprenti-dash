# Cloud Deploy with AWS

If you are planning to deploy to the cloud, the best way to do so is with Elastic Beanstalk and Code Pipeline.

Elastic Beanstalk allows you to easily set up your project on an AWS EC2 instance, and Code Pipeline allows you to 
continuously deploy new versions of your project directly from github.  

As for the database, this project uses Postgres SQL, so we recommend using RDS. 

## Deploy to RDS/EC2

To set up the RDS and EC2, you can use [this guide](https://github.com/codefellows/seattle-java-401d4/blob/master/RDSCheatSheet.md).
 
 **Warning:** This guide says to set Public Accessibility to "Yes." Do not do this. Give your app permissions via IAM
  roles to avoid leaking personal information.  

## CI/CD - Deployment Process
We have partially automated our deployment process using CodePipeline to listen to changes on the master branch of our GitHub repo. However, we are still manually building the application.jar file used by EB. These are the steps we took each time we deployed our current working (Development) branch.
1. Merge all working branches into the development branch.
2. Pull development branch, test to make sure the app is working.
3. Edit application.properties, comment in/out the appropriate lines for deployed app vs. running app locally.
4. In console, run ./gradlew bootJar
5. Copy /build/libs/application.jar into the root level of the repo, replacing the old application.jar file there.
6. Merge these changes back into the GitHub development branch.
7. Merge Github development branch into GitHub master branch. Now, CodePipeline will detect the changes to master branch, pass the repo to EB, and EB will deploy the new application.jar file to an EC2 instance.
8. Finally, everybody pulls master to master on their local machines, and pulls development to development to avoid merge conflicts.


## SNS/SES Setup

Coming soon!!


