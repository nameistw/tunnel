package com.tunnel.server;
  
import java.util.ArrayList;
import java.util.List;

import com.tunnel.common.Constant;
import com.tunnel.common.StringUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;  
  
public class TunnelS2CServerHandler extends SimpleChannelInboundHandler<String>{  
    public static final AttributeKey<String> CLIEN_HOSTS_KEY = AttributeKey.valueOf("client.host"); 
    
      
    @Override  
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {  
        cause.printStackTrace();  
        Attribute<String> attr = ctx.attr(CLIEN_HOSTS_KEY);
        if(attr.get() != null){
        	String hosts = attr.get();
        	String[] hostAry = hosts.split(",");
        	for(String host:hostAry){
        		TunnelManager.remove(host);
        	}
        }
        ctx.close();
    }
    
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String content) throws Exception {
		//注册信息
		if (StringUtil.isNotEmpty(content) || content.contains(Constant.SPLIT_FLAG)) {
			String[] split = content.split(Constant.SPLIT_FLAG);
			String clientName = split[0];
			String hostAry = split[1];
			String[] split2 = hostAry.split(",");

			List<String> successHostList = new ArrayList<>();
			List<String> failedHostList = new ArrayList<>();
			int hostIndex = 0;
			for (String host : split2) {
				host = host == null ? "" : host.trim();
				Tunnel tunnel = new Tunnel(host, clientName, hostIndex++);
				Attribute<String> attr = ctx.attr(CLIEN_HOSTS_KEY);
				if(attr.get() == null){
					attr.setIfAbsent(hostAry);
				}
				tunnel.setChannelHandlerContext(ctx);
				boolean ok = TunnelManager.add(tunnel);
				if (ok) {
					successHostList.add(host);
				} else {
					failedHostList.add(host);
				}
			}
			String success = "";
			for (String host : successHostList) {
				if (StringUtil.isNotEmpty(success)) {
					success = success + ",";
				}
				success = success + host;
			}
			String failed = "";
			for (String host : failedHostList) {
				if (StringUtil.isNotEmpty(failed)) {
					failed = failed + ",";
				}
				failed = failed + host;
			}
		}
	}  
  
}  