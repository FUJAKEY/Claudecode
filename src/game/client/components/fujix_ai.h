#ifndef GAME_CLIENT_COMPONENTS_FUJIX_AI_H
#define GAME_CLIENT_COMPONENTS_FUJIX_AI_H

#include <game/client/component.h>

class CFujixAI : public CComponent
{
public:
	void OnRender() override;
	void OnInit() override;

private:
	void UpdateControls();
};

#endif
