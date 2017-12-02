package com.lpmoon.spring.property.zookeeper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by lpmoon on 17/12/2.
 */
public class ZKClient implements Watcher{

    protected final Log logger = LogFactory.getLog(getClass());

    private String zkHost;
    private int sessionTimeout;
    private ZooKeeper zk;

    public ZKClient(String zkHost, int sessionTimeout) {
        this.zkHost = zkHost;
        this.sessionTimeout = sessionTimeout;
    }

    public String get(String path) {
        return get(path, "UTF-8");
    }

    public String get(String path, String encode) {
        byte[] data = get0(path);
        if (data == null) {
            return null;
        }

        try {
            return new String(data, encode);
        } catch (UnsupportedEncodingException e) {
            logger.error("exception occur: ", e);
        }

        return new String(data, Charset.forName("UTF-8"));
    }

    public byte[] get0(String path) {
        Stat stat = stat(path, false);
        try {
            return this.zk.getData(path, false, stat);
        } catch (KeeperException e) {
            if (e.code() == KeeperException.Code.NONODE) {
                return null;
            }

            logger.error("exception occur: ", e);
        } catch (InterruptedException e) {
            logger.error("exception occur: ", e);
        }

        return null;
    }

    public List<String> getChildren(String path, boolean watch) {
        try {
            return this.zk.getChildren(path, watch);
        } catch (Exception e) {
            logger.error("exception occur: ", e);
            return null;
        }
    }

    public boolean set(String path, byte[] data) {
        Stat stat = stat(path, false);
        if (stat == null) {
            logger.info("path:" + path + " not exist");
            // TODO check concurrent create condition ?
            return createNode(path, data, ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
        }

        try {
            this.zk.setData(path, data, -1);
            return true;
        } catch (KeeperException e) {
            logger.error("exception occur: ", e);
        } catch (InterruptedException e) {
            logger.error("exception occur: ", e);
        }

        return false;
    }

    public boolean createNode(String path, byte[] data, List<ACL> acl, CreateMode createMode) {
        try {
            this.zk.create(path, data, acl, createMode);
        } catch (Exception e) {
            logger.error("exception occur: ", e);
            return false;
        }

        return true;
    }

    public boolean createNode(String path, String data, List<ACL> acl, CreateMode createMode) {
        return createNode(path, data, acl, createMode);
    }

    private boolean exists(String path, boolean watch) {
        Stat stat = stat(path, watch);
        if (stat == null) {
            return false;
        }

        return true;
    }

    public Stat stat(String path, boolean watch) {
        Stat stat = null;
        try {
            stat = this.zk.exists(path, watch);
        } catch (KeeperException e) {
            logger.error("exception occur: ", e);
        } catch (InterruptedException e) {
            logger.error("exception occur: ", e);
        }

        return stat;
    }

    public void start() {
        synchronized (this) {
            if (this.zk == null) {
                try {
                    this.zk = new ZooKeeper(this.zkHost, this.sessionTimeout, this);
                } catch (IOException e) {
                    logger.error("exception occur: ", e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void close() {
        synchronized (this) {
            if (this.zk != null) {
                try {
                    this.zk.close();
                } catch (InterruptedException e) {
                    logger.error("exception occur: ", e);
                }

                this.zk = null; // gc
            }
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        logger.info("enter process() method...event = " + watchedEvent);

        if (watchedEvent == null) {
            return;
        }

        Event.KeeperState keeperState = watchedEvent.getState();
        Event.EventType eventType = watchedEvent.getType();
        String path = watchedEvent.getPath();

        logger.info(String.format("Connection State：%s", keeperState));
        logger.info(String.format("Event Type：%s", eventType));
        logger.info(String.format("Path：%s", path));

        if (Event.KeeperState.SyncConnected == keeperState) {
            if (Event.EventType.None == eventType) {
                logger.info("Success connect to server...");
            } else if (Event.EventType.NodeCreated == eventType) {
                logger.info("Success create node");
                this.exists(path, true);
            } else if (Event.EventType.NodeDataChanged == eventType) {
                logger.info("Success update data");
                logger.info("Node data: " + this.get(path));
            } else if (Event.EventType.NodeChildrenChanged == eventType) {
                logger.info("Child nodes change");
                logger.info("Child nodes：" + this.getChildren(path, true));
            } else if (Event.EventType.NodeDeleted == eventType) {
                logger.info("Child node" + path + " delete");
            }
        } else if (Event.KeeperState.Disconnected == keeperState) {
            logger.info("Disconnec to server");
        } else if (Event.KeeperState.AuthFailed == keeperState) {
            logger.info("Auth fail");
        } else if (Event.KeeperState.Expired == keeperState) {
            logger.info("Session expire");
        }
    }
}
