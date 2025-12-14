# 日志

## 11.28 下午

之前做的游戏引擎实在整不下去了，工作量太大而且涉及到太多我压根不会的东西，所以就算了，换项目

准备做个类似图吧工具箱的东西，可以方便烤机之类的操作，方便诊所工作

## 11.29 凌晨

找到个叫LibreHardwareMonitor的开源项目用来收集传感器信息，furmark2可以使用命令行操作，这样就解决了自动化操作的大部分问题。感觉这个项目会简单很多，我再看看JavaFX就行

不过LibreHardwareMonitor是C#写的，LibreHardwareMonitorLib怎么没有文档，那我怎么用🤪

## 11.29 下午

让chatgpt生成了个wrapper封装了LibreHardwareMonitorLib，但是返回的数据没有CPU温度和功率，GitHub上查了查issue，说是因为管理员权限的问题，用管理员就能出电压了，但是也不出温度

又查了下，有些数据被华硕主板设置为不可访问，我需要研究研究g-helper是怎么读到他们的。另外拯救者也有类似的设计，看来需要对一些牌子做额外适配

不对啊，在LibreHardwareMonitor的UI里是能看到CPU温度和功率的，显然是封装有问题。让AI也没调好，看库的C#代码也没看明白人家是怎么调用的，怪了

github上找到一个人做了LibreHardwareMonitor的封装，但是他怎么也不写文档，没看明白怎么用

不过风扇转速这样的东西确实是是被华硕藏起来的，必须得研究研究g-helper

你们为什么不写文档啊？？？

## 11.30 晚上

喜讯，在github上找到了一个叫fan-control的rust项目，它为了调用LibreHardwareMonitor，自己用C#写了一个wrapper，是通过tcp来实现通信的，然后在rust里调用。我会rust，把相关rust
代码翻译成Java应该就能拿到数据了。

我永远爱rust

## 12.1 晚上

确实如此。但是它的wrapper只存温度和风扇转速，所以我还需要自己改改wrapper。照猫画虎写C#应该问题不大

但是不知道为什么我自己编译的运行不了，说是缺runtime，虽然我确定我装了并且环境变量也是对的

## 12.2 下午

认真研究了研究rider上的配置，发现需要改四处地方才能使用正确的.net版本把wrapper编译到正确的地方。原本版本的wrapper只收集temperature信息，其它全过滤了。我需要自己改一下C#代码，让它把全部信息都传过来

## 12.2 晚上

爆改C#成功了。现在我收到了一个两千多行的Hardware list，json格式的，包含的太全面了

我觉得原本那个rust项目的wrapper确实在设计上有问题：wrapper应该本本分分地返回原始数据，关于数据过滤筛选之类的逻辑不应在wrapper里

但是我观察了这个list，发现起码一千行是关于网速之类的，确实是没必要的数据。把它们在wrapper就过滤掉确实很爽，但是我讨厌这样做。考虑到只是在本机内传输数据，这一千行对性能影响不大

但是java这边需要遍历这个json，这一千行确实会造成性能问题，我想想怎么避免它无脑遍历

## 12.4 晚上

HardwareInfoManager的constructor和update()里都需要处理硬件信息。同一个索引在这两个地方对应的名字是不一样的，假如手动写一一对应的代码没什么技术含量但维护负担太大

所以我起初考虑写一个Map，然后用反射。但是反射遍历起来未免也太慢了，update()最好是x ms级的

所以最后干脆用python写了个脚本（resources/sensor_map.py）生成java代码。只需要维护脚本里那个表就行了。现在constructor和update()里巨长的if-else就是用它生成的，乐

另外就是CPU、GPU这些类里成员变量很多，起初想用lombok简化Getter和Setter，但是不知道为什么过不了编译，干脆就用IDEA生成了一堆Getter和Setter，以后再折腾这个

写了个test，现在的update()平均需要跑2ms，感觉还有优化空间（虽然完全够用了）

## 12.9 上午

最近几天干了这些事情：

试着用JavaFX写界面，但是如果是修改IDEA默认给出的配置文件，把gui和kernel放到平级，gui就会报找不到javafx runtime，死活修不好，所以先不做界面了

写了烤鸡和判定的方法，为此复习了Java多线程的各种知识。但是不知道为什么程序全部运行完主线程不自行停止，查了process explorer也没看明白有什么没有杀掉的线程

## 12.10 凌晨

今天在写磁盘和注册表相关的东西。需要用到diskpart，是用process的BufferedWriter和BufferedReader交互的。这东西不太聪明，最开始的实现会卡住，认真看了这俩的文档发现我的用法有问题。后来明明已经consume了最开始的几行但是它仍旧会被读到。于是尝试了一些小妙招但是要么又卡住，要么返回空字符串，不会解决了

想着一个char一个char读来debug，没想到这样就能得到正确结果了，莫名其妙解决问题了

值得一提的是，这个过程中我一直在问sonnet 4.5，但是它给的方法并无什么用。让它debug十分钟不如我自己写几个println来debug两分钟。感觉是我没有掌握正确用法导致的，不然这也太菜了

## 12.12 晚上

用class来模拟硬件是正确的设计，这样能充分利用面向对象的作用。今天实现了Disk，比较顺利

现在还差一键windows和office激活要写。诊所的标准方法是用`irm https://get.activated.win | iex`，我现在需要知道它的原理

我在这个工具的官网上找到了irm和iex的作用介绍：`The IRM command in PowerShell downloads a script from a specified URL, and the IEX command executes it.`，所以不执行iex，把前半段重定向到某个文件就可以把脚本下载下来了，是个powerhshell脚本，它折腾半天最后实际上又下载了一个叫MAS_AIO的cmd脚本，这个脚本在官网就能找到，这才是激活工具本体

## 12.14 晚上

脚本语言我看不太懂，所以把那个激活工具丢给了sonnect 4.5，它给出了脚本的启动参数，这就很方便了，很顺畅地实现了获取系统信息和操作这个脚本的相关方法
