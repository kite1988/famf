## 1. Dataset: Image Tweets for Recommendation [(12.3MB)](rating.zip)
           
We crawled the dataset for image tweet recommendation task in a user-centric manner. That is, we first sampled 926 Twitter users, and then crawled their retweeted/non-retweeted image tweets. As restricted by Twitter policy, we anonymized user's identity and released post ID only. With the post IDs, you can further pull the acutal posts via Twitter API. Read our [tutorial](instructions.MD) on using the API.
	  
	  
Each line contains the rating history for a specific user. Take the following snippet of a line as an example. "user_1" is the anonymized user ID, "481049212336226304" is the post ID, and 1/0 indicates that "user_1" has retweeted/non-retweeted the tweet.
	   
``` user_1,481049212336226304 0,412686436283211777 0,456501293100441601 0,470904716936085504 0,461920492258328577 0,480756504258039809 0,483177102947409920 0,433005021924708352 0,462479190500335616 0,456137444476604418 0,436983489963892736 0,446020735044435969 0,464113493692940288 0,456098157231734784 0,486188626360156160 0,463040926286643201 0,464396874976608256 0,439411779307704320 0,489779224082132992 0,455761012906422272 1,...```


## 2. Dataset: Twitter images with manually-recognized text [(60KB)](ocr.zip)
To understand the text nature of Twitter images, we randomly sampled 500 images and identified the embedded text from the images. To the date of 20/08/2016, however, 26 images are no longer available on Twitter (e.g., deleted by user). Thus, we released the annotations for the remaining  474 image tweets.

Similary, with the post IDs, you can further pull the acutal posts via Twitter API. Read our [tutorial](instructions.MD) on using the API.
															  
	 
In image_label.txt, each line contains the image tweet ID, the URL of the image and the category of images. See below as an example.
``` 451841123510935552,https://pbs.twimg.com/media/BkVDG1yIgAA3Y-4.jpg,tweet-screenshot ```
	
															  
There are six image categories, namely, 
* text-style: image consists purely of text.
* meme-style: image overlaid with text. 
* tweet-screenshot: screenshot of other tweet.*
* other: other synthetic images that contain text. *
* natural: natural scene with text.*
* no-text: the image has no embedded text.*

For images with embedded text, you can further find their text (mannually annotated) in <i>text</i> fold of the released dataset. This dataset could be used to benchmark the performance of OCR tools on Twitter images.</p>
	
	
