/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bankxmltranslator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Buhrkall
 */
@XmlType(factoryMethod="newInstance")
@XmlRootElement(name= "LoanRequest")
public class Result {
    
@XmlElement
private final String ssn;

@XmlElement
private final int creditScore;

@XmlElement
private final double loanAmount;

@XmlElement
private final int loanDuration;


public Result(String ssn, int creditScore,double loanAmount, int loanDuration){
this.ssn = ssn;
this.creditScore = creditScore;
this.loanAmount = loanAmount;
this.loanDuration = loanDuration;
}


private Result(){
this.ssn = null;
this.creditScore = 0;
this.loanAmount = 0;
this.loanDuration = 0;
}

public static Result newInstance() {
        return new Result();
    }

    
//<LoanRequest>
//<ssn>12345678</ssn>
//<creditScore>685</creditScore>
//<loanAmount>1000.0</loanAmount>
//<loanDuration>1973-01-01 01:00:00.0 CET</loanDuration>
//</LoanRequest>
  
}
