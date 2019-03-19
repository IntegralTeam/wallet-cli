package org.tron.common.zksnark.merkle;

import com.google.protobuf.ByteString;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tron.core.capsule.IncrementalMerkleTreeCapsule;
import org.tron.core.capsule.IncrementalMerkleVoucherCapsule;
import org.tron.core.db.Manager;
import org.tron.core.capsule.SHA256CompressCapsule;
import org.tron.common.zksnark.merkle.IncrementalMerkleVoucherContainer.OutputPointUtil;

@Slf4j
public class MerkleContainer {

  @Setter
  @Getter
  private Manager manager;

  public static MerkleContainer createInstance(Manager manager) {
    MerkleContainer instance = new MerkleContainer();
    instance.setManager(manager);
    return instance;
  }

  public static byte[] lastTreeKey = "LAST_TREE".getBytes();
  public static byte[] currentTreeKey = "CURRENT_TREE".getBytes();

  public IncrementalMerkleTreeContainer getCurrentMerkle() {
    IncrementalMerkleTreeCapsule capsule = manager.getMerkleTreeStore().get(currentTreeKey);
    if (capsule == null) {
      return getBestMerkle();
    }
    return capsule.toMerkleTreeContainer();
  }

  public IncrementalMerkleTreeContainer getBestMerkle() {
    IncrementalMerkleTreeCapsule capsule = manager.getMerkleTreeStore().get(lastTreeKey);
    if (capsule == null) {
      return null;
    }
    return capsule.toMerkleTreeContainer();
  }

  public void saveCurrentMerkleTreeAsBestMerkleTree() {
    IncrementalMerkleTreeContainer treeContainer = getCurrentMerkle();
    setBestMerkle(treeContainer);
    putMerkleTreeIntoStore(treeContainer.getMerkleTreeKey(), treeContainer.getTreeCapsule());
  }

  public void setBestMerkle(IncrementalMerkleTreeContainer treeContainer) {
    manager.getMerkleTreeStore().put(lastTreeKey, treeContainer.getTreeCapsule());
  }

  public void setCurrentMerkle(IncrementalMerkleTreeContainer treeContainer) {
    manager.getMerkleTreeStore().put(currentTreeKey, treeContainer.getTreeCapsule());
  }


  public boolean merkleRootIsExist(byte[] rt) {
    return this.manager.getMerkleTreeStore().contain(rt);
  }

  public IncrementalMerkleTreeCapsule getMerkleTree(byte[] rt) {
    return this.manager.getMerkleTreeStore().get(rt);
  }

  public IncrementalMerkleTreeContainer saveCmIntoMerkleTree(
      IncrementalMerkleTreeContainer tree, byte[] cm) {

    SHA256CompressCapsule sha256CompressCapsule1 = new SHA256CompressCapsule();
    sha256CompressCapsule1.setContent(ByteString.copyFrom(cm));
    tree.append(sha256CompressCapsule1.getInstance());

    return tree;
  }

  //todo : to delete later
  @Deprecated
  public IncrementalMerkleTreeContainer saveCmIntoMerkleTree(
      byte[] rt, byte[] cm1, byte[] cm2, byte[] hash) {

    IncrementalMerkleTreeContainer tree =
        this.manager.getMerkleTreeStore().get(rt).toMerkleTreeContainer();

    tree = saveCmIntoMerkleTree(tree, cm1);

    IncrementalMerkleVoucherContainer witnessContainer1 =
        tree.getTreeCapsule().deepCopy().toMerkleTreeContainer().toWitness();

    tree = saveCmIntoMerkleTree(tree, cm2);

    witnessContainer1 = saveCmIntoMerkleWitness(witnessContainer1, cm2);

    IncrementalMerkleVoucherContainer witnessContainer2 = tree.toWitness();

    witnessContainer1.getWitnessCapsule().setOutputPoint(ByteString.copyFrom(hash), 0);
    putMerkleWitnessIntoStore(
        witnessContainer1.getMerkleWitnessKey(), witnessContainer1.getWitnessCapsule());
    witnessContainer2.getWitnessCapsule().setOutputPoint(ByteString.copyFrom(hash), 1);
    putMerkleWitnessIntoStore(
        witnessContainer2.getMerkleWitnessKey(), witnessContainer2.getWitnessCapsule());

    putMerkleTreeIntoStore(tree.getMerkleTreeKey(), tree.getTreeCapsule());
    return tree;
  }

  public IncrementalMerkleVoucherContainer saveCmIntoMerkleWitness(
      IncrementalMerkleVoucherContainer tree, byte[] cm) {
    SHA256CompressCapsule sha256CompressCapsule1 = new SHA256CompressCapsule();
    sha256CompressCapsule1.setContent(ByteString.copyFrom(cm));
    tree.append(sha256CompressCapsule1.getInstance());

    return tree;
  }

  public void putMerkleTreeIntoStore(byte[] key, IncrementalMerkleTreeCapsule capsule) {
    this.manager.getMerkleTreeStore().put(key, capsule);
  }

  public void putMerkleWitnessIntoStore(byte[] key, IncrementalMerkleVoucherCapsule capsule) {
    this.manager.getMerkleWitnessStore().put(key, capsule);
  }

  public MerklePath merklePath(byte[] rt) {
    IncrementalMerkleTreeContainer tree =
        this.manager.getMerkleTreeStore().get(rt).toMerkleTreeContainer();
    return tree.path();
  }

  public IncrementalMerkleVoucherCapsule getWitness(byte[] hash, int index) {
    return this.manager.getMerkleWitnessStore().get(OutputPointUtil.outputPointToKey(hash, index));
  }
}
