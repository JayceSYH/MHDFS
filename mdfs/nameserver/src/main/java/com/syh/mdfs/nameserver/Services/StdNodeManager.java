package com.syh.mdfs.nameserver.Services;

import Config.ApplicationConfig;
import Config.NameServerConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.syh.mdfs.nameserver.Models.MDFSFileNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class StdNodeManager implements NodeManager, Runnable{
    @Resource
    private EurekaClient eurekaClient;
    @Autowired
    private FileManager fileManager;
    private Map<String, MDFSFileNode> nodeMapping = new HashMap<>();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private StdNodeManager() {
        new Thread(this).start();
    }

    public void run() {
        while (true) {
            try {
                if (eurekaClient != null && fileManager != null) {
                    Application nodeApp = eurekaClient.getApplication(ApplicationConfig.NodeServerApp);
                    if (nodeApp != null)
                        this.resetNodes(nodeApp.getInstances());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(NameServerConfig.eurekaCheckCycle);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void resetNodes(List<InstanceInfo> instanceInfos) {
        Map<String, MDFSFileNode> newNodeMapping = new HashMap<>();

        this.readWriteLock.readLock().lock();
        try {
            for (InstanceInfo instanceInfo : instanceInfos) {
                if (!this.nodeMapping.keySet().contains(instanceInfo.getInstanceId()) ||
                        !this.nodeMapping.get(instanceInfo.getInstanceId()).
                                checkRestartByUpTime(instanceInfo.getLeaseInfo().getServiceUpTimestamp())) {
                    MDFSFileNode MDFSFileNode = new MDFSFileNode(instanceInfo.getIPAddr(), instanceInfo.getPort(),
                            instanceInfo.getInstanceId(), instanceInfo.getLeaseInfo().getServiceUpTimestamp());
                    this.fileManager.resetFileMapping(MDFSFileNode);

                    newNodeMapping.put(instanceInfo.getInstanceId(), MDFSFileNode);
                } else {

                    newNodeMapping.put(instanceInfo.getInstanceId(), this.nodeMapping.get(instanceInfo.getInstanceId()));
                }
            }

            for (String instanceId : this.nodeMapping.keySet())
                if (!newNodeMapping.containsKey(instanceId))
                    fileManager.notifyNodeDeprecated(this.nodeMapping.get(instanceId));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }

        this.readWriteLock.writeLock().lock();
        this.nodeMapping = newNodeMapping;
        this.readWriteLock.writeLock().unlock();

        this.readWriteLock.readLock().lock();
        System.out.println(this.nodeMapping);
        this.readWriteLock.readLock().unlock();
    }

    @Override
    public List<MDFSFileNode> getAvailableNodesByLoad() {
        List<MDFSFileNode> MDFSFileNodes = new ArrayList<>();

        this.readWriteLock.readLock().lock();
        try {
            for (String key : this.nodeMapping.keySet()) {
                MDFSFileNodes.add(nodeMapping.get(key));
            }

            MDFSFileNodes.sort(Comparator.comparingInt(MDFSFileNode::getBlockCount));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }

        return MDFSFileNodes;
    }
}
