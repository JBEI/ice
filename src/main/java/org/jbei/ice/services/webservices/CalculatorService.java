package org.jbei.ice.services.webservices;

import javax.jws.WebService;

@WebService
public class CalculatorService {
    public int add(int a, int b) {
        return a + b;
    }
}
