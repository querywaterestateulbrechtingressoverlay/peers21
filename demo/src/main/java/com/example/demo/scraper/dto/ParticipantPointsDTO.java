package com.example.demo.scraper.dto;

import com.example.demo.data.ApiPeerPointsData;

public record ParticipantPointsDTO(
  int peerReviewPoints,
  int codeReviewPoints,
  int coins) {
  public ApiPeerPointsData toTableForm(Integer id) {
    return new ApiPeerPointsData(id, peerReviewPoints, codeReviewPoints, coins);
  }
}
