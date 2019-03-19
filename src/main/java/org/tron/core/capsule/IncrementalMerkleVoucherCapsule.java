package org.tron.core.capsule;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.tron.protos.Contract.IncrementalMerkleVoucher;
import org.tron.protos.Contract.OutputPoint;
import org.tron.protos.Contract.SHA256Compress;
import org.tron.common.zksnark.merkle.IncrementalMerkleVoucherContainer;

@Slf4j
public class IncrementalMerkleVoucherCapsule implements ProtoCapsule<IncrementalMerkleVoucher> {

  private IncrementalMerkleVoucher voucher;


  public IncrementalMerkleVoucherCapsule() {
    voucher = IncrementalMerkleVoucher.getDefaultInstance();
  }

  public IncrementalMerkleVoucherCapsule(IncrementalMerkleVoucher voucher) {
    this.voucher = voucher;
  }

  public IncrementalMerkleVoucherCapsule(final byte[] data) {
    try {
      this.voucher = IncrementalMerkleVoucher.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      log.debug(e.getMessage(), e);
    }
  }


  public IncrementalMerkleTreeCapsule getTree() {
    return new IncrementalMerkleTreeCapsule(this.voucher.getTree());
  }

  public void setTree(IncrementalMerkleTreeCapsule merkleTreeCapsule) {
    this.voucher = this.voucher.toBuilder().setTree(merkleTreeCapsule.getInstance()).build();
  }

  public List<SHA256Compress> getFilled() {
    return this.voucher.getFilledList();
  }

  public void addFilled(SHA256Compress value) {
    this.voucher = this.voucher.toBuilder().addFilled(value).build();
  }

  public IncrementalMerkleTreeCapsule getCursor() {
    return new IncrementalMerkleTreeCapsule(this.voucher.getCursor());
  }

  public void clearCursor() {
    this.voucher = this.voucher.toBuilder().clearCursor().build();
  }

  public void setCursor(IncrementalMerkleTreeCapsule cursor) {
    this.voucher = this.voucher.toBuilder().setCursor(cursor.getInstance()).build();
  }

  public long getCursorDepth() {
    return this.voucher.getCursorDepth();
  }

  public void setCursorDepth(long cursorDepth) {
    this.voucher = this.voucher.toBuilder().setCursorDepth(cursorDepth).build();
  }

  public void resetRt() {
    this.voucher = this.voucher.toBuilder().setRt(toMerkleVoucherContainer().root().getContent())
        .build();
  }

  public OutputPoint getOutputPoint() {
    return this.voucher.getOutputPoint();
  }

  public void setOutputPoint(ByteString hash, int index) {
    this.voucher = this.voucher.toBuilder()
        .setOutputPoint(OutputPoint.newBuilder().setHash(hash).setIndex(index).build())
        .build();
  }

  @Override
  public byte[] getData() {
    return this.voucher.toByteArray();
  }

  @Override
  public IncrementalMerkleVoucher getInstance() {
    return this.voucher;
  }

  public IncrementalMerkleVoucherContainer toMerkleVoucherContainer() {
    return new IncrementalMerkleVoucherContainer(this);
  }

  public int size() {
    return getTree().toMerkleTreeContainer().size() + getFilled().size() + getCursor()
        .toMerkleTreeContainer().size();
  }

  public void printSize() {
    System.out.println(
        "TreeSize:" + getTree().toMerkleTreeContainer().size() + ",FillSize:" + getFilled().size()
            + ",CursorSize:" + getCursor().toMerkleTreeContainer().size());
  }
}
