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

// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  //////////
  features: {
    home: true,
    tc: true,
    nettest: true,
    statistics: true,
    map: true,
    history: true,
    testresults: true,
    help: true,
    opendata: true,
    about: false,
    footer: true,
    settings: true,
    docu: false
  },
  landing_page: 'home',
  user_agent: 'abc',
  languages: ['en', 'de'],
  servers: {
    //control: 'http://localhost:8080/api/v1/'
    //control: 'http://localhost:18080/api/v1/',
    //result: 'http://localhost:8082/api/v1/',
    //search: 'http://localhost:8083/api/v1/',
    //map: 'http://localhost:8084/api/v1/',
    //statistic: 'http://localhost:8085/api/v1/',
    control: 'https://controller-de-01.net-neutrality.tools/api/v1/',
    result: 'https://result-de-01.net-neutrality.tools/api/v1/',
    search: 'https://search-de-01.net-neutrality.tools/api/v1/',
    map: 'https://map-de-01.net-neutrality.tools/api/v0/',
    statistic: 'https://statistic-de-01.net-neutrality.tools/api/v1/'
  },
  keys: {
    google: ''
  },
  classificationColorStyle: 'background',
  colors: {
    groups: {
      pink: ['#da7883', '#d16b74', '#a65056', '#8a3d43'],
      orange: ['#ffce99', '#ffab65', '#ff8118', '#e67416'],
      purple: ['#e097ff', '#bb8ed8', '#a34ed8', '#69328b'],
      turquoise: ['#c8ebef', '#90bcbf', '#739c9e', '#64878a'],
      green: ['#bfdb2d', '#a6bf27', '#8a9f21', '#71821b']
    },
    gauge: {
      arc_background: '#EFEFEF',
      arc_inner: '#921F56',
      arc_outer: '#29348A',
      fontName: 'arial',
      font: '#FFFFFF'
    }
  },
  nettest: {
    custom_tc: false,
    tag: null,
    tests: {
      qos: false,
      ndt: true,
      rmbt: true
    }
  },
  deserializeTypes: {
    registrationRequestDeserializeType:
      'at.alladin.nettest.shared.berec.collector.api.v1.dto.agent.registration.RegistrationRequest',
    settingsRequestDeserializeType:
      'at.alladin.nettest.shared.berec.collector.api.v1.dto.agent.settings.SettingsRequest',
    speedMeasurementPeerRequestDeserializeType:
      'at.alladin.nettest.shared.berec.collector.api.v1.dto.peer.SpeedMeasurementPeerRequest'
  },
  map: {
    showLegend: true,
    view: {
      default_map: 'google',
      map_provider: ['google', 'googleSatellite', 'googleTerrain', 'googleHybrid', 'osm'],
      position: [46.879966, 12.726909],
      zoom_initial: 5,
      zoom_min: 1,
      zoom_max: 20,
      hybrid: {
        zoom_level: 15
      }
    },
    filter_defaults: {
      map_options: 'all/download'
    },
    options: {
      statistical_method: [],
      map_options: [],
      period: [],
      technology: [],
      operator: [],
      layer_type: [
        { value: 'hybrid', name: 'MAP.SELECT.LAYER.HYBRID', unit: '', default: true },
        { value: 'heatmap', name: 'MAP.SELECT.LAYER.HEATMAP', unit: '' },
        { value: 'points', name: 'MAP.SELECT.LAYER.POINTS', unit: '' }
      ]
    }
  },
  opendata: {
    startDate: {
      year: 2019,
      month: 9
    }
  },
  statistics: {},
  user: {
    allow_query_uuid: true,
    allow_set_uuid: false,
    shown: {
      force_ip4: true,
      invisible: false,
      anonymous_mode: true,
      no_anonymize_before_delete_user: false,
      delete_user: true,
      client_uuid: true,
      measurement_selection: true,
      measurement_selection_speed: true,
      measurement_selection_qos: true
    }
  },
  socialMediaSettings: {
    history: {
      medias: ['FACEBOOK', 'TWITTER', 'MAIL', 'WHATSAPP']
    }
  }
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
import 'zone.js/dist/zone-error'; // Included with Angular CLI.
