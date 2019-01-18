## 链接海马玩  
adb connect 127.0.0.1:26944  

adb shell am start -D -S -W com.alimama.moon/.ui.WizardActivity  
adb shell am start -D -S -W com.taobao.taobao/com.taobao.ju.android.ui.main.TabMainActivity  
com.taobao.search.common.chitu.ChituPanelActivity  


adb shell "ps | grep com.alimama.moon"  
adb shell "ps | grep com.taobao.taobao"  

adb forward tcp:8800 jdwp:23678  

复制这条信息，打开??一淘App??即可看到【 北极绒男士秋衣秋裤纯棉毛衫女士薄款保暖内衣加大码打底情侣套装】￥DlqBGH2hwN￥ ??淘口令??  

## 查看手机连接  
adb devices  
## 将frida-server复制到手机  
adb -s emulator-5554 push frida-server /data/local/tmp/  
## 多台手机连接下选择其中一个连接  
adb -s emulator-5554 shell  
## 进入手机cmd模式  
adb shell   
## 授予权限  
chmod 777  frida-server-12.2.27-linux-x86_64  
## 运行服务  
./frida-server &  
## 端口转发  
adb forward tcp:27042 tcp:27042   
adb forward tcp:27043 tcp:27043  
## 测试  
frida-ps -R  
