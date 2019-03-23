package edu.ucu.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.18.0)",
    comments = "Source: raft.proto")
public final class ClusterNodeGrpc {

  private ClusterNodeGrpc() {}

  public static final String SERVICE_NAME = "helloworld.ClusterNode";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<edu.ucu.proto.VoteRequest,
      edu.ucu.proto.VoteResponse> getRequestVoteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RequestVote",
      requestType = edu.ucu.proto.VoteRequest.class,
      responseType = edu.ucu.proto.VoteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<edu.ucu.proto.VoteRequest,
      edu.ucu.proto.VoteResponse> getRequestVoteMethod() {
    io.grpc.MethodDescriptor<edu.ucu.proto.VoteRequest, edu.ucu.proto.VoteResponse> getRequestVoteMethod;
    if ((getRequestVoteMethod = ClusterNodeGrpc.getRequestVoteMethod) == null) {
      synchronized (ClusterNodeGrpc.class) {
        if ((getRequestVoteMethod = ClusterNodeGrpc.getRequestVoteMethod) == null) {
          ClusterNodeGrpc.getRequestVoteMethod = getRequestVoteMethod = 
              io.grpc.MethodDescriptor.<edu.ucu.proto.VoteRequest, edu.ucu.proto.VoteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "helloworld.ClusterNode", "RequestVote"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  edu.ucu.proto.VoteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  edu.ucu.proto.VoteResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ClusterNodeMethodDescriptorSupplier("RequestVote"))
                  .build();
          }
        }
     }
     return getRequestVoteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<edu.ucu.proto.AppendRequest,
      edu.ucu.proto.AppendResponse> getAppendEntriesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AppendEntries",
      requestType = edu.ucu.proto.AppendRequest.class,
      responseType = edu.ucu.proto.AppendResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<edu.ucu.proto.AppendRequest,
      edu.ucu.proto.AppendResponse> getAppendEntriesMethod() {
    io.grpc.MethodDescriptor<edu.ucu.proto.AppendRequest, edu.ucu.proto.AppendResponse> getAppendEntriesMethod;
    if ((getAppendEntriesMethod = ClusterNodeGrpc.getAppendEntriesMethod) == null) {
      synchronized (ClusterNodeGrpc.class) {
        if ((getAppendEntriesMethod = ClusterNodeGrpc.getAppendEntriesMethod) == null) {
          ClusterNodeGrpc.getAppendEntriesMethod = getAppendEntriesMethod = 
              io.grpc.MethodDescriptor.<edu.ucu.proto.AppendRequest, edu.ucu.proto.AppendResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "helloworld.ClusterNode", "AppendEntries"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  edu.ucu.proto.AppendRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  edu.ucu.proto.AppendResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ClusterNodeMethodDescriptorSupplier("AppendEntries"))
                  .build();
          }
        }
     }
     return getAppendEntriesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ClusterNodeStub newStub(io.grpc.Channel channel) {
    return new ClusterNodeStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ClusterNodeBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ClusterNodeBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ClusterNodeFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ClusterNodeFutureStub(channel);
  }

  /**
   */
  public static abstract class ClusterNodeImplBase implements io.grpc.BindableService {

    /**
     */
    public void requestVote(edu.ucu.proto.VoteRequest request,
        io.grpc.stub.StreamObserver<edu.ucu.proto.VoteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRequestVoteMethod(), responseObserver);
    }

    /**
     */
    public void appendEntries(edu.ucu.proto.AppendRequest request,
        io.grpc.stub.StreamObserver<edu.ucu.proto.AppendResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getAppendEntriesMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRequestVoteMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                edu.ucu.proto.VoteRequest,
                edu.ucu.proto.VoteResponse>(
                  this, METHODID_REQUEST_VOTE)))
          .addMethod(
            getAppendEntriesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                edu.ucu.proto.AppendRequest,
                edu.ucu.proto.AppendResponse>(
                  this, METHODID_APPEND_ENTRIES)))
          .build();
    }
  }

  /**
   */
  public static final class ClusterNodeStub extends io.grpc.stub.AbstractStub<ClusterNodeStub> {
    private ClusterNodeStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClusterNodeStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClusterNodeStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterNodeStub(channel, callOptions);
    }

    /**
     */
    public void requestVote(edu.ucu.proto.VoteRequest request,
        io.grpc.stub.StreamObserver<edu.ucu.proto.VoteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRequestVoteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void appendEntries(edu.ucu.proto.AppendRequest request,
        io.grpc.stub.StreamObserver<edu.ucu.proto.AppendResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAppendEntriesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ClusterNodeBlockingStub extends io.grpc.stub.AbstractStub<ClusterNodeBlockingStub> {
    private ClusterNodeBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClusterNodeBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClusterNodeBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterNodeBlockingStub(channel, callOptions);
    }

    /**
     */
    public edu.ucu.proto.VoteResponse requestVote(edu.ucu.proto.VoteRequest request) {
      return blockingUnaryCall(
          getChannel(), getRequestVoteMethod(), getCallOptions(), request);
    }

    /**
     */
    public edu.ucu.proto.AppendResponse appendEntries(edu.ucu.proto.AppendRequest request) {
      return blockingUnaryCall(
          getChannel(), getAppendEntriesMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ClusterNodeFutureStub extends io.grpc.stub.AbstractStub<ClusterNodeFutureStub> {
    private ClusterNodeFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClusterNodeFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClusterNodeFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterNodeFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<edu.ucu.proto.VoteResponse> requestVote(
        edu.ucu.proto.VoteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRequestVoteMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<edu.ucu.proto.AppendResponse> appendEntries(
        edu.ucu.proto.AppendRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getAppendEntriesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REQUEST_VOTE = 0;
  private static final int METHODID_APPEND_ENTRIES = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ClusterNodeImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ClusterNodeImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REQUEST_VOTE:
          serviceImpl.requestVote((edu.ucu.proto.VoteRequest) request,
              (io.grpc.stub.StreamObserver<edu.ucu.proto.VoteResponse>) responseObserver);
          break;
        case METHODID_APPEND_ENTRIES:
          serviceImpl.appendEntries((edu.ucu.proto.AppendRequest) request,
              (io.grpc.stub.StreamObserver<edu.ucu.proto.AppendResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ClusterNodeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ClusterNodeBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return edu.ucu.proto.Raft.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ClusterNode");
    }
  }

  private static final class ClusterNodeFileDescriptorSupplier
      extends ClusterNodeBaseDescriptorSupplier {
    ClusterNodeFileDescriptorSupplier() {}
  }

  private static final class ClusterNodeMethodDescriptorSupplier
      extends ClusterNodeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ClusterNodeMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ClusterNodeGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ClusterNodeFileDescriptorSupplier())
              .addMethod(getRequestVoteMethod())
              .addMethod(getAppendEntriesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
