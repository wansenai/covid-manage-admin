package com.summer.common.core;

import com.summer.common.support.CommonCode;
import com.summer.common.helper.CollectsHelper;
import com.summer.common.helper.StringHelper;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** 异常信息 **/
public class RpcException extends RuntimeException {
    private final ICodeMSG icm;
    private final List<String> packs;

    public final Throwable cause;

    public RpcException(ICodeMSG icm) {
        super(icm.message());
        this.icm = icm;
        this.cause = null;
        this.packs = new ArrayList<>();
    }

    public RpcException(Pair<Integer, String> cm) {
        super(message(cm));
        this.icm = icm(cm);
        this.cause = null;
        this.packs = new ArrayList<>();
    }

    public RpcException(ICodeMSG icm, List<String> packs) {
        super(icmOfMsg(icm, packs));
        this.icm = icm;
        this.cause = null;
        this.packs = packs;
    }

    public RpcException(Pair<Integer, String> cm, List<String> packs) {
        super(icmOfMsg(cm, packs));
        this.icm = icm(cm);
        this.cause = null;
        this.packs = packs;
    }

    public RpcException(ICodeMSG icm, Throwable cause) {
        super(icm.message(), cause);
        this.icm = icm;
        this.cause = cause;
        this.packs = new ArrayList<>();
    }

    public RpcException(Pair<Integer, String> cm, Throwable cause) {
        super(message(cm), cause);
        this.icm = icm(cm);
        this.cause = cause;
        this.packs = new ArrayList<>();
    }

    public RpcException(ICodeMSG icm, Throwable cause, List<String> packs) {
        super(icmOfMsg(icm, packs));
        this.icm = icm;
        this.cause = cause;
        this.packs = packs;
    }

    public RpcException(Pair<Integer, String> cm, Throwable cause, List<String> packs) {
        super(icmOfMsg(cm, packs));
        this.icm = icm(cm);
        this.cause = cause;
        this.packs = packs;
    }

    public int code () {
        return null == icm ? CommonCode.SuccessOk.code() : icm.code();
    }

    public String msg() {
        return RpcException.icmOfMsg(icm, packs);
    }

    @Deprecated
    public Optional<ICodeMSG> icm() {
        return Optional.ofNullable(icm);
    }

    private static ICodeMSG icm(Pair<Integer, String> cm) {
        return ICodeMSG.create(cm.getKey(), cm.getValue());
    }

    private static String message(Pair<Integer, String> cm) {
        return cm.getKey() + " -> " + cm.getValue();
    }

    private static String icmOfMsg(ICodeMSG icm, List<String> packs) {
        if(null == icm) {
            return StringHelper.EMPTY;
        }
        if(CollectsHelper.isNullOrEmpty(packs)) {
            return icm.msg();
        }
        return String.format(icm.msg(), packs.toArray());
    }

    private static String icmOfMsg(Pair<Integer, String> cm, List<String> packs) {
        if(null == cm) {
            return StringHelper.EMPTY;
        }
        if(CollectsHelper.isNullOrEmpty(packs)) {
            return cm.getValue();
        }
        return String.format(cm.getValue(), packs.toArray());
    }
}
