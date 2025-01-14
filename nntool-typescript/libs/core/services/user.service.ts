/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NGXLogger } from 'ngx-logger';

import { TranslateService } from '@ngx-translate/core';
import { forkJoin, Observable, Observer, throwError } from 'rxjs';

import { ConfigService } from './config.service';
import { RequestsService } from './requests.service';
import { BrowserStorageService } from './storage.service';
import { WebsiteSettings } from '../models/settings/settings.interface';

export class UserInfo {
  public static fromJson(json: string): UserInfo {
    const obj: any = JSON.parse(json);
    const userInfo: UserInfo = new UserInfo();
    if (typeof obj.disassociated !== 'undefined') {
      userInfo.disassociated = obj.disassociated;
    }
    if (typeof obj.invisible !== 'undefined') {
      userInfo.invisible = obj.invisible;
    }
    if (typeof obj.forceIp !== 'undefined') {
      userInfo.forceIp = obj.forceIp;
    }
    if (typeof obj.disassociateBeforeDelete !== 'undefined') {
      userInfo.disassociateBeforeDelete = obj.disassociateBeforeDelete;
    }
    if (typeof obj.uuid !== 'undefined') {
      userInfo.uuid = obj.uuid;
    }
    if (typeof obj.measurementUUIDs !== 'undefined') {
      userInfo.measurementUUIDs = obj.measurementUUIDs;
    }
    if (typeof obj.acceptTC !== 'undefined') {
      userInfo.acceptTC = obj.acceptTC;
    }
    if (typeof obj.executeDownloadMeasurement !== 'undefined') {
      userInfo.executeDownloadMeasurement = obj.executeDownloadMeasurement;
    }
    if (typeof obj.executePingMeasurement !== 'undefined') {
      userInfo.executePingMeasurement = obj.executePingMeasurement;
    }
    if (typeof obj.executeUploadMeasurement !== 'undefined') {
      userInfo.executeUploadMeasurement = obj.executeUploadMeasurement;
    }
    if (typeof obj.executeQosMeasurement !== 'undefined') {
      userInfo.executeQosMeasurement = obj.executeQosMeasurement;
    }

    if (typeof obj.enableLoopMode !== 'undefined') {
      userInfo.enableLoopMode = obj.enableLoopMode;
    }

    return userInfo;
  }
  /**
   * Disassociate measurements after taking them
   * - keep user id
   */
  public disassociated = false;

  /**
   * Don't track me
   * - includes disassociate?
   * - delete user id after each test
   */
  public invisible = false;

  /**
   * Useful?
   */
  public forceIp = false;

  /**
   *
   */
  public disassociateBeforeDelete = false;

  /**
   * Client uuid
   */
  public uuid: string = null;

  public measurementUUIDs: string[] = null;

  /**
   * Accepted terms and conditions
   */
  public acceptTC = false;

  public executePingMeasurement = true;

  public executeDownloadMeasurement = true;

  public executeUploadMeasurement = true;

  public executeQosMeasurement = true;

  public enableLoopMode = false;

  public apply(other: UserInfo): void {
    this.disassociated = other.disassociated;
    this.invisible = other.invisible;
    this.forceIp = other.forceIp;
    this.disassociateBeforeDelete = other.disassociateBeforeDelete;
    this.uuid = other.uuid;
    this.measurementUUIDs = other.measurementUUIDs;
    this.acceptTC = other.acceptTC;
    this.executeDownloadMeasurement = other.executeDownloadMeasurement;
    this.executePingMeasurement = other.executePingMeasurement;
    this.executeUploadMeasurement = other.executeUploadMeasurement;
    this.executeQosMeasurement = other.executeQosMeasurement;
    this.enableLoopMode = other.enableLoopMode;
  }

  public toJson(): string {
    const obj: any = {
      disassociated: this.disassociated,
      invisible: this.invisible,
      forceIp: this.forceIp,
      disassociateBeforeDelete: this.disassociateBeforeDelete,
      uuid: this.uuid,
      measurementUUIDs: this.measurementUUIDs,
      acceptTC: this.acceptTC,
      executeDownloadMeasurement: this.executeDownloadMeasurement,
      executePingMeasurement: this.executePingMeasurement,
      executeUploadMeasurement: this.executeUploadMeasurement,
      executeQosMeasurement: this.executeQosMeasurement,
      enableLoopMode: this.enableLoopMode
    };

    return JSON.stringify(obj);
  }
}

@Injectable()
export class UserService {
  get user(): UserInfo {
    if (this.userInfo === null) {
      this.userInfo = new UserInfo();
      this.load(this.userInfo);
    }
    return this.userInfo;
  }

  set user(other: UserInfo) {
    if (this.userInfo === null) {
      this.userInfo = new UserInfo();
      this.load(this.userInfo);
    }
    this.userInfo.apply(other);
  }

  private userInfo: UserInfo = null;
  private config: WebsiteSettings;

  constructor(
    private logger: NGXLogger,
    private storage: BrowserStorageService,
    private requests: RequestsService,
    private configService: ConfigService,
    private translateService: TranslateService
  ) {
    this.config = this.configService.getConfig();
  }

  /**
   * Load user information from local storage and if found apply it to user
   *
   * @param user  Object to load info into (if not set -> use local userInfo)
   */
  public load(user: UserInfo = null): void {
    /*
        user.disassociated = this.getKeyDefault("disassociated", user.disassociated);
        user.invisible = this.getKeyDefault("invisible", user.invisible);
        user.forceIp = this.getKeyDefault("forceIp", user.forceIp);
        user.disassociateBeforeDelete = this.getKeyDefault("disassociateBeforeDelete", user.disassociateBeforeDelete);
        */
    if (!user) {
      user = this.user;
    }
    const userInfoSt: string = this.storage.load('user_info', true);
    if (userInfoSt) {
      user.apply(UserInfo.fromJson(userInfoSt));
    }
  }

  /**
   * Save user information to local storage
   *
   * @param user  User to save (if not set -> use local userInfo)
   */
  public save(user: UserInfo = null): void {
    /*
        this.storage.save("disassociated", this.userInfo.disassociated, true);
        this.storage.save("invisible", this.userInfo.invisible, true);
        this.storage.save("forceIp", this.userInfo.forceIp, true);
        this.storage.save("disassociateBeforeDelete", this.userInfo.disassociateBeforeDelete, true);
        */
    if (!user) {
      user = this.user;
    }
    this.storage.save('user_info', user.toJson(), true);
  }

  /**
   * Load measurements (uuids) of user
   *
   * @param user  User to load measurements for (if not set -> use local userInfo)
   * @returns Was the call successful
   */
  public loadMeasurements(user: UserInfo = null): Observable<any> {
    if (!user) {
      user = this.user;
    }
    if (!user || !user.uuid) {
      return throwError('No user set');
    }
    // const lang: string = this.translateService.currentLang;
    return new Observable((observer: any) => {
      this.requests
        .getJson<any>(
          // TODO: take result-service url from settings request
          Location.joinWithSlash(this.config.servers.result, 'measurement-agents/' + user.uuid + '/measurements'),
          {}
        )
        .subscribe(
          (data: any) => {
            this.logger.debug('User Mes', data);
            user.measurementUUIDs = [];

            for (const mes of data.data.content) {
              user.measurementUUIDs.push(mes.test_uuid);
            }
            observer.next(data);
          },
          (err: HttpErrorResponse) => {
            this.logger.error('Error retrieving measurements', err);
            observer.error(err);
          },
          () => {
            observer.complete();
          }
        );
    });
  }

  public loadMeasurementDetail(measurementUuid: string, user: UserInfo = null): Observable<any> {
    if (!user) {
      user = this.user;
    }
    if (!user || !user.uuid) {
      return throwError('No user set');
    }

    return new Observable((observer: any) => {
      this.requests
        .getJson<any>(
          // TODO: take result-service url from settings request
          Location.joinWithSlash(
            this.config.servers.result,
            'measurement-agents/' + user.uuid + '/measurements/' + measurementUuid + '/details'
          ),
          {}
        )
        .subscribe(
          (data: any) => {
            this.logger.debug('User measurement details: ', data);

            observer.next(data);
          },
          (error: HttpErrorResponse) => {
            this.logger.error('Error retrieving measurement', error);
            observer.error();
          },
          () => observer.complete()
        );
    });
  }

  public loadFullMeasurement(measurementUuid: string, user: UserInfo = null): Observable<any> {
    if (!user) {
      user = this.user;
    }
    if (!user || !user.uuid) {
      return throwError('No user set');
    }

    return new Observable((observer: any) => {
      this.requests
        .getJson<any>(
          // TODO: take result-service url from settings request
          Location.joinWithSlash(
            this.config.servers.result,
            'measurement-agents/' + user.uuid + '/measurements/' + measurementUuid
          ),
          {}
        )
        .subscribe(
          (data: any) => {
            this.logger.debug('User full measurement: ', data);

            observer.next(data);
          },
          (error: HttpErrorResponse) => {
            this.logger.error('Error retrieving measurement', error);
            observer.error();
          },
          () => observer.complete()
        );
    });
  }

  /**
   * Disassociate a measurement from user
   *
   * @param clientUUID        UUID of client to use
   * @param measurementUUID   UUID of measurement to use
   * @param wait              Wait for completion of api call or return immediatelly
   * @returns Was the call successfull
   */
  public disassociate(clientUUID: string, measurementUUID: string, wait: boolean = true): Observable<any> {
    if (clientUUID == null || !clientUUID) {
      this.logger.error('No client uuid');
      return throwError('No client uuid');
    }
    if (measurementUUID == null || !measurementUUID) {
      this.logger.error('No measurement uuid');
      return throwError('No measurement uuid');
    }

    return this.requests.deleteJson<any>(
      Location.joinWithSlash(
        this.config.servers.result,
        'measurement-agents/' + clientUUID + '/measurements/' + measurementUUID
      )
    );
  }

  /**
   * Disassociate all measurements for user
   *
   * (Uses only measurement uuid present in user.measurementUUIDs)
   *
   * @param user  User to disassociate
   */
  public disassociateAll(user: UserInfo = null): Observable<any> {
    if (!user) {
      user = this.user;
    }

    if (!user || !user.uuid) {
      this.logger.error('No valid user given');
      return throwError('No valid user given');
    }

    return this.requests.deleteJson<any>(Location.joinWithSlash(
      this.config.servers.result,
      'measurement-agents/' + user.uuid + '/measurements'
    ));
  }

  private getKeyDefault(key: string, defaultValue: boolean): boolean {
    let res: any = this.storage.load(key);
    if (res === null || typeof res === 'undefined') {
      res = defaultValue;
    } else if (typeof res === 'string') {
      res = res === 'true';
    }
    return res;
  }
}
