/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.deployment;

import org.jboss.as.deployment.module.DeploymentModuleLoader;
import org.jboss.as.deployment.module.DeploymentModuleLoaderImpl;
import org.jboss.as.deployment.module.DeploymentModuleLoaderSelector;
import org.jboss.as.deployment.processor.ModuleConfgProcessor;
import org.jboss.as.deployment.processor.ModuleDependencyProcessor;
import org.jboss.as.deployment.processor.ModuleDeploymentProcessor;
import org.jboss.as.deployment.processor.ServiceDeploymentParsingProcessor;
import org.jboss.as.deployment.processor.ServiceDeploymentProcessor;
import org.jboss.as.deployment.test.PassthroughService;
import org.jboss.as.deployment.test.TestModuleDependencyProcessor;
import org.jboss.as.deployment.test.TestServiceDeployment;
import org.jboss.as.deployment.unit.DeploymentChain;
import org.jboss.as.deployment.unit.DeploymentChainImpl;
import org.jboss.as.deployment.unit.DeploymentChainProcessorInjector;
import org.jboss.as.deployment.unit.DeploymentChainService;
import org.jboss.as.deployment.unit.DeploymentUnitProcessor;
import org.jboss.as.deployment.unit.DeploymentUnitProcessorService;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoaderSelector;
import org.jboss.msc.service.BatchBuilder;
import org.jboss.msc.service.BatchServiceBuilder;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.TimingServiceListener;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test case to do some basic Service deployment functionality checking.
 * 
 * @author John E. Bailey
 */
public class ServiceDeploymentTestCase {
    private static final ServiceName DEPLOYMENT_MANAGER_NAME = ServiceName.JBOSS.append("deployment", "manager");
    private static final ServiceName CHAIN_SERVICE_NAME = ServiceName.JBOSS.append("deployment", "chain", "service");
    private static final ServiceName DEPLOYMENT_MODULE_LOADER_SERVICE_NAME = ServiceName.JBOSS.append("deployment", "module", "loader");

    @Test
    public void testServiceDeployment() throws Exception {
        Module.setModuleLoaderSelector(new DeploymentModuleLoaderSelector());
        final ServiceContainer serviceContainer = ServiceContainer.Factory.create();
        final DeploymentManagerImpl deploymentManager = setupDeploymentManger(serviceContainer);
        setupProcessors(serviceContainer);
        final DeploymentResult result = deploymentManager.deploy(initializeDeployment()).getDeploymentResult();
        assertNotNull(result);
        assertEquals(DeploymentResult.Result.SUCCESS, result.getResult());

        final ServiceController<?> testServiceController = serviceContainer.getService(TestServiceDeployment.TEST_SERVICE_NAME);
        assertNotNull(testServiceController);
        assertEquals(ServiceController.State.UP, testServiceController.getState());
        serviceContainer.shutdown();
    }

    private DeploymentManagerImpl setupDeploymentManger(final ServiceContainer serviceContainer) throws Exception {
        final DeploymentChain deploymentChain = new DeploymentChainImpl("deployment.chain.service");
        final DeploymentModuleLoader deploymentModuleLoader = new DeploymentModuleLoaderImpl(ModuleLoaderSelector.DEFAULT.getCurrentLoader());
        final DeploymentManagerImpl deploymentManager = new DeploymentManagerImpl(serviceContainer) {
            @Override
            protected DeploymentChain determineDeploymentChain(VirtualFile deploymentRoo) {
                return deploymentChain;
            }

            @Override
            protected DeploymentModuleLoader determineDeploymentModuleLoader(VirtualFile deploymentRoo) {
                return deploymentModuleLoader;
            }
        };

        final BatchBuilder builder = serviceContainer.batchBuilder();
        final CountDownLatch latch = new CountDownLatch(1);
        final TimingServiceListener listener = new TimingServiceListener(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });
        builder.addListener(listener);

        builder.addService(CHAIN_SERVICE_NAME, new DeploymentChainService(deploymentChain));
        builder.addService(DEPLOYMENT_MODULE_LOADER_SERVICE_NAME, new PassthroughService(deploymentModuleLoader));

        final BatchServiceBuilder deploymentManagerServiceBuilder = builder.addService(DEPLOYMENT_MANAGER_NAME, deploymentManager);
        deploymentManagerServiceBuilder.addDependency(CHAIN_SERVICE_NAME);
        deploymentManagerServiceBuilder.addDependency(DEPLOYMENT_MODULE_LOADER_SERVICE_NAME);
        builder.install();
        listener.finishBatch();
        latch.await(1L, TimeUnit.SECONDS);
        if(!listener.finished())
            fail("Did not install deployment manager within 1 second.");

        if(!ServiceController.State.UP.equals(serviceContainer.getService(DEPLOYMENT_MANAGER_NAME).getState()))
            Thread.sleep(100L);
        assertEquals(ServiceController.State.UP, serviceContainer.getService(DEPLOYMENT_MANAGER_NAME).getState());
        return deploymentManager;
    }

    private void setupProcessors(final ServiceContainer serviceContainer) throws Exception {
        final BatchBuilder builder = serviceContainer.batchBuilder();
        final CountDownLatch latch = new CountDownLatch(1);
        final TimingServiceListener listener = new TimingServiceListener(new Runnable() {
            public void run() {
                latch.countDown();
            }
        });
        builder.addListener(listener);

        addProcessor(builder, ServiceName.JBOSS.append("deployment", "processor", "module", "dependency"), new ModuleDependencyProcessor(), ModuleDependencyProcessor.PRIORITY);
        addProcessor(builder, ServiceName.JBOSS.append("deployment", "processor", "module", "dependency", "test"), new TestModuleDependencyProcessor(), TestModuleDependencyProcessor.PRIORITY);
        addProcessor(builder, ServiceName.JBOSS.append("deployment", "processor", "module", "config"), new ModuleConfgProcessor(), ModuleConfgProcessor.PRIORITY);
        addProcessor(builder, ServiceName.JBOSS.append("deployment", "processor", "module", "deployment"), new ModuleDeploymentProcessor(), ModuleDeploymentProcessor.PRIORITY);
        addProcessor(builder, ServiceName.JBOSS.append("deployment", "processor", "service", "parser"), new ServiceDeploymentParsingProcessor(), ServiceDeploymentParsingProcessor.PRIORITY);
        addProcessor(builder, ServiceName.JBOSS.append("deployment", "processor", "service", "deployment"), new ServiceDeploymentProcessor(), ServiceDeploymentProcessor.PRIORITY);

        builder.install();
        listener.finishBatch();
        latch.await(1L, TimeUnit.SECONDS);
        if(!listener.finished())
            fail("Did not install processors within 1 seconds");
    }

    private <T extends DeploymentUnitProcessor> DeploymentUnitProcessorService<T> addProcessor(final BatchBuilder builder, final ServiceName serviceName, final T deploymentUnitProcessor, final long priority) {
        final DeploymentUnitProcessorService<T> deploymentUnitProcessorService = new DeploymentUnitProcessorService<T>(deploymentUnitProcessor);
        builder.addService(serviceName, deploymentUnitProcessorService)
            .addDependency(CHAIN_SERVICE_NAME).toInjector(new DeploymentChainProcessorInjector<T>(deploymentUnitProcessorService, priority));
        return deploymentUnitProcessorService;
    }

    private VirtualFile initializeDeployment() throws Exception {
        final VirtualFile virtualFile = VFS.getChild(getResource("/test/deploymentOne"));
        copyResource("/org/jboss/as/deployment/test/TestServiceDeployment.class", "/test/deploymentOne", "org/jboss/as/deployment/test");
        return virtualFile;
    }

    protected URL getResource(final String path) throws Exception {
        return ServiceDeploymentTestCase.class.getResource(path);
    }

    protected File getResourceFile(final String path) throws Exception {
        return new File(getResource(path).toURI());
    }

    protected void copyResource(final String inputResource, final String outputBase, final String outputPath) throws Exception {
        final File resource = getResourceFile(inputResource);
        final File outputDirectory = new File(getResourceFile(outputBase), outputPath);

        if(!resource.exists())
            throw new IllegalArgumentException("Resource does not exist");
        if(outputDirectory.exists() && outputDirectory.isFile())
            throw new IllegalArgumentException("OutputDirectory must be a directory");
        if(!outputDirectory.exists()) {
            if(!outputDirectory.mkdirs())
                throw new RuntimeException("Failed to create output directory");
        }
        final File outputFile = new File(outputDirectory, resource.getName());
        final InputStream in = new FileInputStream(resource);
        try {
            final OutputStream out = new FileOutputStream(outputFile);
            try {
                final byte[] b = new byte[8192];
                int c;
                while((c = in.read(b)) != -1) {
                    out.write(b, 0, c);
                }
                out.close();
                in.close();
            } finally {
                VFSUtils.safeClose(out);
            }
        } finally {
            VFSUtils.safeClose(in);
        }
    }

}
