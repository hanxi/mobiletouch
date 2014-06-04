mobiletouch
===========

移动触控板的安卓客户端，将安卓移动设备作为触控板，发送指令到服务器

----

使用http协议发送指令,通过url参数确定命令。

url = "http://192.168.16.14:8080/"

----

## 双指滑动

* 滑动方向
     MOVE_LEFT  = 0;
     MOVE_RIGHT = 1;
     MOVE_UP    = 2;
     MOVE_DOWN  = 3;

* 向左:发送指令
    > cmd = doublemove?direction=0&distance=724  

    > doublemove 标识双指移动

    > direction 表示方向

    > distance 标识滑动距离

* 向右
    > cmd = doublemove?direction=1&distance=724  

* 向上
    > cmd = doublemove?direction=2&distance=724  

* 向下
    > cmd = doublemove?direction=3&distance=724  

## 服务器搭建

* 计划使用python搭建，因为python可以跨平台且实现http服务器很简单。可以修改 https://github.com/hanxi/py_http 来实现
