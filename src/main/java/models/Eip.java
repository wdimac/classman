package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.amazonaws.services.ec2.model.Address;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="eip")
public class Eip {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long id;

  private String region;
  private String description;
  @Column(name="instance_id")
  private String instanceId;
  @Column(name="public_ip")
  private String publicIp;
  @Column(name="allocation_id")
  private String allocationId;
  @Column(name="association_id")
  private String associationId;
  private String domain;
  @Column(name="network_interface_id")
  private String networkInterfaceId;
  @Column(name="network_interface_owner_id")
  private String networkInterfaceOwnerId;
  @Column(name="private_ip_address")
  private String privateIpAddress;

  @ManyToOne
  @JoinColumn(name="pool_user_id", updatable=false)
  @JsonIgnoreProperties({"eips"})
  private User poolUser;

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getInstanceId() {
    return instanceId;
  }
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }
  public String getPublicIp() {
    return publicIp;
  }
  public void setPublicIp(String publicIp) {
    this.publicIp = publicIp;
  }
  public String getAllocationId() {
    return allocationId;
  }
  public void setAllocationId(String allocationId) {
    this.allocationId = allocationId;
  }
  public String getAssociationId() {
    return associationId;
  }
  public void setAssociationId(String associationId) {
    this.associationId = associationId;
  }
  public String getDomain() {
    return domain;
  }
  public void setDomain(String domain) {
    this.domain = domain;
  }
  public String getNetworkInterfaceId() {
    return networkInterfaceId;
  }
  public void setNetworkInterfaceId(String networkInterfaceId) {
    this.networkInterfaceId = networkInterfaceId;
  }
  public String getNetworkInterfaceOwnerId() {
    return networkInterfaceOwnerId;
  }
  public void setNetworkInterfaceOwnerId(String networkInterfaceOwnerId) {
    this.networkInterfaceOwnerId = networkInterfaceOwnerId;
  }
  public String getPrivateIpAddress() {
    return privateIpAddress;
  }
  public void setPrivateIpAddress(String privateIpAddress) {
    this.privateIpAddress = privateIpAddress;
  }
  public String getRegion() {
    return region;
  }
  public void setRegion(String region) {
    this.region = region;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public User getPoolUser() {
    return poolUser;
  }
  public void setPoolUser(User poolUser) {
    this.poolUser = poolUser;
  }
  public void loadFromAddress(Address address) {
    this.setAllocationId(address.getAllocationId());
    this.setAssociationId(address.getAssociationId());
    this.setDomain(address.getDomain());;
    this.setInstanceId(address.getInstanceId());
    this.setNetworkInterfaceId(address.getNetworkInterfaceId());
    this.setNetworkInterfaceOwnerId(address.getNetworkInterfaceOwnerId());
    this.setPrivateIpAddress(address.getPrivateIpAddress());
    this.setPublicIp(address.getPublicIp());
  }

}
