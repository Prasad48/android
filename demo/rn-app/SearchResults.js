

import React, { Component } from 'react'
import {
  StyleSheet,
  Image,
  View,
  TouchableHighlight,
  FlatList,
  Text,
} from 'react-native';

class ListItem extends React.PureComponent {
  _onPress = () => {
    this.props.onPressItem(this.props.index);
  }

  render() {
    const item = this.props.item;
    const price = item.price_formatted.split(' ')[0];
    return (
      <TouchableHighlight
        testID='touchablehighlight'
        onPress={this._onPress}
        underlayColor='#dddddd'>
        <View testID='view_row'>
          <View testID='view_container' style={styles.rowContainer}>
            <Image testID='image' style={styles.thumb} source={{ uri: item.img_url }} />
            <View testID='view_price_title' style={styles.textContainer}>
              <Text testID='text_price' style={styles.price}>{price}</Text>
              <Text testID='text_title' style={styles.title}
                numberOfLines={1}>{item.title}</Text>
            </View>
          </View>
          <View testID='view_separator' style={styles.separator} />
        </View>
      </TouchableHighlight>
    );
  }
}

export class SearchResults extends Component {
  static navigationOptions = {
    title: 'Results',
  };

  _keyExtractor = (item, index) => index.toString();

  _renderItem = ({item, index}) => (
    <ListItem
      item={item}
      index={index}
      onPressItem={this._onPressItem}
    />
  );

  _onPressItem = (index) => {
    console.log("Pressed row: "+index);
  }

  render() {
    const { params } = this.props.navigation.state;
    return (
      <FlatList
        data={params.listings}
        keyExtractor={this._keyExtractor}
        renderItem={this._renderItem}
      />
    );
  }
}

const styles = StyleSheet.create({
  thumb: {
    width: 80,
    height: 80,
    marginRight: 10
  },
  textContainer: {
    flex: 1
  },
  separator: {
    height: 1,
    backgroundColor: '#dddddd'
  },
  price: {
    fontSize: 25,
    fontWeight: 'bold',
    color: '#48BBEC'
  },
  title: {
    fontSize: 20,
    color: '#656565'
  },
  rowContainer: {
    flexDirection: 'row',
    padding: 10
  },
});