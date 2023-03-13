package org.kunlab.internalkpm.upgrader.mocks;

import org.kunlab.kpm.ExceptionHandler;

public class ExceptionHandlerMock implements ExceptionHandler
{
    @Override
    public void report(Throwable throwable)
    {
        throwable.printStackTrace();
    }
}
