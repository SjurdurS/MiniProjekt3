/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package miniprojekt3;

/**
 *
 * @author Nicolai
 */
class NodeTuple {
    
    private  int port;
    private String hostName;
    
    public NodeTuple(int port, String hostName){
        
        this.port = port;
        this.hostName = hostName;
        
    }
    
    public int getPort(){
        return port;
    }
    
    public String getHostName(){
        return hostName;
    }
    
    @Override
    public boolean equals(Object obj){
        
        if(this == obj){
            return true;
        }
        NodeTuple n = (NodeTuple)obj;
        if(n.getHostName().equals(this.hostName) && n.getPort() == this.port){
            return true;
        }
        return false;
    }
    
    public int hashCode(){
        
    }
    
   
}
