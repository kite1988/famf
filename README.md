# Feature-aware Matrix Factorization Model

This is the implementation based on the following paper:

Tao Chen, Xiangnan He and Min-Yen Kan (2016). [Context-aware Image Tweet Modelling and Recommendation.](https://www.comp.nus.edu.sg/~kanmy/papers/mm16.pdf) In Proceedings of the 24th ACM International Conference on Multimedia (MM'16), Amsterdam, The Netherlands.

We have additionally released the datasets used in our paper. See [data/README.MD] (data/README.MD) for details.


**Please cite our MM'16 paper if you use our code or dataset. Thanks!** 

Author: Tao Chen (http://www.cs.jhu.edu/~taochen)

## Usage

* Change the configuration in [text_visual.conf](https://github.com/kite1988/famf/blob/master/conf/text_visual.conf)
* Run java command:

  ``` java main.TextVisualMain.java conf/text_visual.conf```
  dddd
  If you are training with a large dataset, please allocate more memory to JVM, e.g.,
  
   ``` java -Xmx2g main.TextVisualMain.java conf/text_visual.conf```
ddd
  This code invokes the pipeline of training, testing and evaluation. The evaluation is conducted for a personalized image tweet recommendation task. Please see the paper for detailed description.


   
