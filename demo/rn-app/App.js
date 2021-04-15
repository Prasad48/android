import { createStackNavigator } from 'react-navigation';
import { SearchPage } from './SearchPage';
import { SearchResults } from './SearchResults';
import UserIQ from 'useriq-react-native';

// export class App extends React.Component {
//   componentDidMount() {
//     UserIQ.setUser("http://10.0.2.2:2020/sdk");
//     UserIQ.init('fc760695ca023fac62869125c9c71da85672b7bf');
//     UserIQ.setUser({
//         id: "101",
//         name: "Palkesh",
//         email: "palkesh@useriq.com",
//         accountId: "1001",
//         accountName: "Palkesh a/c",
//         signUpDate: "14/08/2018",
//     });
//   }
// }

export const App = createStackNavigator({
  Home: { screen: SearchPage },
  Results: { screen: SearchResults },
});