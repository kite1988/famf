# Feature-aware Matrix Factorization Model

This is the implementation based on the following paper:

Tao Chen, Xiangnan He and Min-Yen Kan (2016). [Context-aware Image Tweet Modelling and Recommendation.](https://www.comp.nus.edu.sg/~kanmy/papers/mm16.pdf) In Proceedings of the 24th ACM International Conference on Multimedia (MM'16), Amsterdam, The Netherlands.

We have additionally released two datasets used in our paper:
* [Image tweet for personalized recommendation](https://github.com/kite1988/famf/blob/master/data/README.MD#1-dataset-image-tweets-for-recommendation-123mb)
* [Twitter images with manually-recognized text](https://github.com/kite1988/famf/blob/master/data/README.MD#2-dataset-twitter-images-with-manually-recognized-text-60kb)


**Please cite our MM'16 paper if you use our code or datasets. Thanks!** 

Author: Tao Chen (http://www.cs.jhu.edu/~taochen)

## Usage

We implemented three feature-aware matrix factorization (FAMF) models that use different features.

Model | Configuration File | Features
------------ | ------------- | -------------
[Text](https://github.com/kite1988/famf/blob/master/src/main/TextMain.java) | [text.conf](https://github.com/kite1988/famf/blob/master/conf/text.conf) | Post's contextual words
[Visual](https://github.com/kite1988/famf/blob/master/src/main/VisualMain.java) | [visual.conf](https://github.com/kite1988/famf/blob/master/conf/visual.conf) | Image's visual tags
[TextVisual](https://github.com/kite1988/famf/blob/master/src/main/TextVisualMain.java) | [text_visual.conf](https://github.com/kite1988/famf/blob/master/conf/text_visual.conf) | The combination of contextual words and visual tags

### Dataset Preparation

If you are using our [dataset](https://github.com/kite1988/famf/blob/master/data/README.MD#1-dataset-image-tweets-for-recommendation-123mb), please:
* Crawl the tweets and images from Twitter
* Extract necessary features
* Generate training and test set

The required input files vary from one model to another. Please refer to model configuration file for details. We list the format of rating and feature file in the below:

* Rating file format

Each line contains one positive tweet and its paired N negative tweets for a particular user. Each rating consists of four elements: user, post ID, publisher (the author of the post), and the rating (1 denotes the user has retweeted the tweet, 0 not retweeted). The negative tweets could be sampled by our time-aware negative sampling algorithm (detailed in the paper).

```user_1 486447896191959040 pub_65893 1,user_1 486619477933838336 pub_18 0,user_1 486596611431477248 pub_22 0,user_1 486602569419333632 pub_21 0,user_1 486532028570275840 pub_45 0...```

* Feature file format

Each line contains the post ID followed by the feature ID, which is the index of contextual words or visual tags. Therfore, feature ID should be continuous integer [0, W), where W is the number of features for a particular type.

```544555137272791040,2275 3474 36361 9123 23694 57 714 3112 1212 19505 7409 8011 18770 5878 256 3314 2039```

### How to run

* Set the configuration file properly
  Please see the comments (start with #) in the configuration file for guidance. In general, you should set the dataset paths and the visual/texual vocabulary size according to your dataset, and tune the model parameters (e.g., number of factors, regularizer) to obtain optimal results. For the rest parameters, you may just the default values.
  
* Complile the source code and run the model

  * If you are using Eclipse:

  Please add the the jar files in "lib" folder to project build path. See this [post](http://www.wikihow.com/Add-JARs-to-Project-Build-Paths-in-Eclipse-(Java)) on how to do this. And run the respective model with its configuration file as program parameter. 

  * If you are using command line:
  ``` 
   mkdir bin
   javac -cp "lib/*" -d bin src/data/* src/main/* src/matrix/* src/model/* src/util/*
   java -cp "lib/*":bin main.<model>.java conf/<model>.conf 
  ```
  Please replace \<model\> by the respective model name, e.g., TextVisualMain

### Output
The above code invokes the pipeline of training, testing and evaluation, and generates the following files:

File/Folder | Description
------------ | -------------
result.csv      | This file contains the overall experimental results on the test set.
result_user.csv | This file contains the user-level experimental results on the test set.
model           | This folder contains the user factor and feature factor learned in the training set.
prediction      | This folder contains the exact score of user and tweet pair.
config.txt      | The experimental configuration settings.
log.txt         | Log information.
   
