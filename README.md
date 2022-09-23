# ConsoleDemo
 Android Process 复用实例

###实时logcat原理
使用观察者模式对流中的message进行监听，仅需对success或error流添加观察者（Observer）即可

###复用Process原理
封装Process以及Process流的操作，并使用单例模式调用，使Process始终保持原子一致性（调用restart方法后会重新唤起一个新的Process，但Process指针不变）

###最后
本项目仅供学习与交流，勿用于非法用途
可以加我Q：2205903933唠唠嗑
