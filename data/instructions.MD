# Crawling Twitter posts by Ids

As Twitter does not permit the third-party release of actual post content, we have only included the IDs of posts in our dataset. With these IDs, the actual post content can be retrieved using Twitter APIs,


* [statuses/show/:id](https://dev.twitter.com/rest/reference/get/statuses/show/%3Aid) -- getting a single tweet per request
* [statues/lookup](https://dev.twitter.com/rest/reference/get/statuses/lookup) -- getting Tweets in bulk (up to 100 per call)


Alternatively, we can use [Twitter library](https://dev.twitter.com/overview/api/twitter-libraries) (a wrapper of Twitter APIs) to retrieve the tweets. The following guide uses the <a href="https://github.com/tweepy/tweepy">tweepy</a> library. Its
documentation can be found [here](https://tweepy.readthedocs.org/en/v3.5.0/).


## Tweepy Library Setup##
To install, run ```pip install tweepy```, preferably in a virtualenv.

##Register an Application
Create a new application on [Twitter's developer site](https://apps.twitter.com/app/new)
When complete, this will bring you to the application's configuration page - click
```Keys and Access Tokens``` to obtain the Consumer Key and Consumer Secret needed.


Ensure that your keys are kept a secret, and are not revealed to the
public. If you are going to make your scripts open-source,
ensure that the keys are not included within.

## Retrieve Posts by ID 

This code instantiates the client library with the consumer key and secret from
before, and uses it to retrieve posts whose IDs have been specified in a list.

```
from tweepy import API, AppAuthHandler

# ENSURE THESE ARE NOT REVEALED TO THE PUBLIC!
CONSUMER_KEY = &quot;YOUR_KEY_HERE&quot;
CONSUMER_SECRET = &quot;YOUR_SECRET_HERE&quot;

# Instantiate the client library.
authenticator = AppAuthHandler(CONSUMER_KEY, CONSUMER_SECRET)
twitter_api = API(auth_handler=authenticator)

# Post IDs to be retrieved.

post_ids = []

for post_id in post_ids:
    # Retrieve the post from the service.
    post = twitter_api.get_status(id=post_id)

    # Tweepy returns an object for each post.
    # Process it here.
    
    print(post)
 ```
