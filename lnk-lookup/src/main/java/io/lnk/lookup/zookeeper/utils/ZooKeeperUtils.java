package io.lnk.lookup.zookeeper.utils;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.common.PathUtils;
import org.apache.zookeeper.data.ACL;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class ZooKeeperUtils {
    public static final int ANY_VERSION = -1;
    public static final ImmutableList<ACL> OPEN_ACL_UNSAFE = ImmutableList.copyOf(Ids.OPEN_ACL_UNSAFE);
    public static final ImmutableList<ACL> EVERYONE_READ_CREATOR_ALL = ImmutableList.<ACL>builder().addAll(Ids.CREATOR_ALL_ACL).addAll(Ids.READ_ACL_UNSAFE).build();

    public static boolean isRetryable(KeeperException e) {
        Preconditions.checkNotNull(e);

        switch (e.code()) {
            case CONNECTIONLOSS:
            case SESSIONEXPIRED:
            case SESSIONMOVED:
            case OPERATIONTIMEOUT:
                return true;

            case RUNTIMEINCONSISTENCY:
            case DATAINCONSISTENCY:
            case MARSHALLINGERROR:
            case BADARGUMENTS:
            case NONODE:
            case NOAUTH:
            case BADVERSION:
            case NOCHILDRENFOREPHEMERALS:
            case NODEEXISTS:
            case NOTEMPTY:
            case INVALIDCALLBACK:
            case INVALIDACL:
            case AUTHFAILED:
            case UNIMPLEMENTED:

            case SYSTEMERROR:
            case APIERROR:

            case OK:

            default:
                return false;
        }
    }

    public static String normalizePath(String path) {
        String normalizedPath = path.replaceAll("//+", "/").replaceFirst("(.+)/$", "$1");
        PathUtils.validatePath(normalizedPath);
        return normalizedPath;
    }

    private ZooKeeperUtils() {}
}
