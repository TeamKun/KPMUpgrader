package org.kunlab.internalkpm.upgrader.mocks;

import lombok.AllArgsConstructor;
import org.kunlab.kpm.hook.interfaces.HookExecutor;
import org.kunlab.kpm.hook.interfaces.HookRecipientList;
import org.kunlab.kpm.hook.interfaces.KPMHook;
import org.kunlab.kpm.hook.interfaces.KPMHookRecipient;
import org.kunlab.kpm.interfaces.KPMRegistry;

@AllArgsConstructor
public class HookExecutorMock implements HookExecutor
{
    private final KPMRegistry registry;

    @Override
    public void runHook(KPMHookRecipient recipient, KPMHook hook)
    {

    }

    @Override
    public void runHook(HookRecipientList recipients, KPMHook hook)
    {

    }

    @Override
    public KPMRegistry getRegistry()
    {
        return this.registry;
    }
}
