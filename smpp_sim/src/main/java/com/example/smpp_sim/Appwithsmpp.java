package com.example.smpp_sim;
import org.smpp.smscsim.*;
public class Appwithsmpp {
    public static void main(String args[]){
        System.out.println("Hi there !");
        try {
            Simulator.main(args);

        }
        catch(Exception e)
        {
            System.out.println(e);

        }

    }
}
