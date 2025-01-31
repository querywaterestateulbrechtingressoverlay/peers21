package com.example.demo.data;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;

public record Peer(String login,
                   String wave,
                   int intensive,
                   int tribeId,
                   PeerState state,
                   int tribePoints,
                   int expValue,
                   int peerReviewPoints,
                   int codeReviewPoints,
                   int coins) {
  public static Peer combine(PeerBaseData bd, PeerMutableData md) {
    return new Peer(bd.login(), bd.wave(), bd.intensive(), bd.tribeId(), md.state(), md.tribePoints(), md.expValue(), md.peerReviewPoints(), md.codeReviewPoints(), md.coins());
  }
}
