import {UserConfigFn} from 'vite';
import {overrideVaadinConfig} from './vite.generated';

const customConfig: UserConfigFn = () => ({
    base: "/",
});

export default overrideVaadinConfig(customConfig);
